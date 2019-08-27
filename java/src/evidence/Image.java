package evidence;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import static java.awt.image.BufferedImage.TYPE_4BYTE_ABGR;

public class Image {

	/**
	 * Константа Q, приблизительно равная 1/255, но немного превышающая её.
	 * С ней можно конвертировать цвета из флоатов в 0xFF и т. п.
	 */
	public static final float Q = 0x1.010102p-8f;

	public final BufferedImage base;
	public final Graphics2D g;
	public final int scale;
	public final WritableRaster r;
	public final int[] data;
	public final int format;
	private final int height;
	private final int width;

	private float[] colors = {1, 1, 1, 1};

	public Image(File file, int scale) throws IOException {
		this.scale = scale;
		BufferedImage img = ImageIO.read(file);
		this.base = img.getType() == TYPE_4BYTE_ABGR ? img : Util.convertColorspace(img, TYPE_4BYTE_ABGR);
		this.g = base.createGraphics();
		this.r = base.getRaster();
		this.height = r.getHeight();
		System.out.println(base.getType());
		this.width = r.getWidth();
		format = r.getNumBands();
		this.data = new int[this.format * height * width];
		r.getPixels(0, 0, width, height, data);
	}

	public void setColor(float red, float green, float blue, float alpha) {
		colors[0] = red;
		colors[1] = green;
		colors[2] = blue;
		colors[3] = alpha;
	}
	public void permuteColor(float red, float green, float blue, float alpha) {
		colors[0] *= red;
		colors[1] *= green;
		colors[2] *= blue;
		colors[3] *= alpha;
	}
	public void setColor(Color color, float alpha) {
		setColor(color.r * Q, color.g * Q, color.b * Q, alpha);
	}

	public int getHeight() {
		return height >> scale;
	}

	public int getWidth() {
		return width >> scale;
	}

	public void rect(int x1, int y1, int x2, int y2, int color) {

		byte[] colorData = new byte[4];
		for (int i = 0; i < 4; i++) {
			colorData[i] = (byte) (color >> 8 * i & 0xFF);
		}

		for (int x = x1, a = 0; x < x2; x++, a++) {
			for (int y = y1, b = 0; y < y2; y++, b++) {
				int offsetImg = (y * width + x) * format;

				float alphaAbove = -1;
				for (int scope = 3; scope >= 0; scope--) {
					if (alphaAbove == -1) alphaAbove = colorData[scope] * Q;
					float colorAbove = colorData[scope] * Q * alphaAbove;
					float colorBelow = data[offsetImg + scope] * Q;
					data[offsetImg + scope] = (int) ((colorAbove + colorBelow * (1 - alphaAbove)) * 255);

				}
			}
		}

	}

	public void pasteFull(Raster texture, float x1, float y1, float x2, float y2) {
		draw(texture, 0, 0, 1, 1, x1, y1, x2, y2, 1);
	}

	/**
	 * Рейтресинг текстурки на изображение
	 * @param t Текстутра (bufferedimage.getRaster())
	 * @param u1 левая координата X в процентах от общей ширины
	 * @param v1 верхняя координата Y в процентах от общей высоты
	 * @param u2 правая координата X в процентах от общей ширины
	 * @param v2 нижняя координата Y в процентах от общей высоты
	 * @param x1 координата X на изображении, на которой будет левый верхний угол текстурки
	 * @param y1 координата Y на изображении, на которой будет левый верхний угол текстурки
	 * @param x2 координата X на изображении, на которой будет правый нижний угол текстурки
	 * @param y2 координата Y на изображении, на которой будет правый нижний угол текстурки
	 * @param trf множитель размера текстурки
	 */
	public void draw(Raster t, double u1, double v1, double u2, double v2, float x1, float y1, float x2, float y2, double trf) {

		int ff = t.getNumBands();

		int w = t.getWidth();
		int h = t.getHeight();

		int tx = (int) (u1 * w);
		int ty = (int) (v1 * h);
		int tw = (int) (u2 * w) - tx;
		int th = (int) (v2 * h) - ty;
		int[] texture = new int[tw * th * ff];
		t.getPixels(tx, ty, tw, th, texture);

		double factor = scale * trf;

		int xmin = (int) (x1 * scale);
		int ymin = (int) (y1 * scale);
		int xmax = (int) (x2 * scale);
		int ymax = (int) (y2 * scale);

		int sizeX = xmax - xmin;
		int sizeY = ymax - ymin;


		for (int x = xmin, a = 0; x < xmax; x++, a++) {
			for (int y = ymin, b = 0; y < ymax; y++, b++) {
				int offsetImg = (y * width + x) * format;
				int rayX = (int) ((double) a / sizeX * tw);
				int rayY = (int) ((double) b / sizeY * th);
				int offsetTxt = (rayY * tw + rayX) * ff;

				if (ff < 4) {
					for (int scope = 0; scope < ff || scope < format; scope++) {
						data[offsetImg + scope] = texture[offsetTxt + scope];
					}
					return;
				}
				float alphaAbove = -1;
				for (int scope = 3; scope >= 0; scope--) {
					float color = texture[offsetTxt + scope] * Q * colors[scope];
//					System.out.println("scope = " + scope + ", color = " + texture[offsetTxt + scope]);
					if (alphaAbove == -1) alphaAbove = color;
					float colorAbove = color * alphaAbove;
					float colorBelow = data[offsetImg + scope] * Q;
					data[offsetImg + scope] = (int) ((colorAbove + colorBelow * (1 - alphaAbove)) * 255);

				}
			}
		}


	}

	public void flush() {
		r.setPixels(0, 0, width, height, data);
	}

	public void save(File file) throws IOException {
		ImageIO.write(base, "PNG", file);
	}


}