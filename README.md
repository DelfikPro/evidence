# Evidence
Генератор подстав для VimeWorld.

## Достоинства в сравнении с PhotoShop
* Быстрая настройка, особенно при нескольких скриншотах.
* Умная система, автоматизирующая как можно больше (Берёт все цвета, префиксы, уровни и т. п. из [VimeWorld Public API](https://vimeworld.github.io/api-docs)).
* Лёгкое управление работой, максимальная кастомизация, поддержка нескольких игр.
* Автоматическая очистка [EXIF-данных](https://exifinfo.org "Штука, которая палит фотошоп") при генерации.
* Код написан без использования платформозависимых библиотек (напр. LWJGL), и может работать на виртуальном выделенном сервере, снабжая игроков подставами за деньги, и при этом игроки не будут иметь доступ к коду программы.

## Недостатки в сравнении с PhotoShop
* Фотошоп сложный, но в умелых руках с ним можно делать куда более высококачественные подставы.
* Рука или предмет в руке - статичная картинка, которую надо создавать самому перед использованием программы.
* Невозможность делать скриншоты, на которые попали другие игроки (Ники над головой не будут отображаться в режиме F1, за исключением BlockParty, там с этим проблем нет)
* Полоса прокрутки чата до сих пор в разработке.
* Палевные EXIF-данные

# Использование
* Скачайте последний [релиз](https://github.com/DelfikPro/Evidence/releases).
  * Изначально там уже имеется готовый конфиг и пара скринов-образцов. 
* Настройте конфиг (evidence.yml) для себя.
* Зайдите на VimeWorld и сделайте скриншот, предварительно нажав `F1`.
* Откройте этот скриншот с помощью файла `start.bat`.
* В папке evidences появится новый файл - это и есть поддельная улика, готовая к отправке на форум.

## Другие способы делать подставы

### Замена языковых файлов
Заменив строчку "Скриншот сохранён как %s" на "§3§l[Гл. Админ] xtrafrancyz§7: §aПейте чай, будьте здоровы" можно далеко превзойти качество и фотошопа, и моего софта.
Единственная причина, по которой этот софт не бесполезен на фоне такой грандиозной уязвимости - незабываемый опыт в разработке для его создателя, и, с небольшой вероятностью,
альтернатива этой уязвимости в случае её фикса способом, известным в народе как "Админы вайма случайно баг".
В действительности, существует далеко не нулевая вероятность, что вместо нормального фикса всех подстав одним махом, боги ваймворлда запретят менять языковые файлы или вроде того.

### Моддинг
Бесполезная на первый взгляд штука.

Верно, пока есть три вышеперечисленных метода, четвёртым никто заниматься не будет, потому что он абсолютно точно не стоит того.

Однако, [рано или поздно](https://youtu.be/R9u4BENr8ig), все трое братьев-мошенников будут устранены, как и любой другой способ подставы в чате.
И тогда придёт он, и начнётся новая эра - эра подстав на багоюз, читы, некорректные постройки на BuildBattle. 
Произойдёт это [не очень скоро](https://youtu.be/VwjyiQzopAA?t=2m54s) даже после фикса подстав в чате, но стоит держать в уме существование этого четвёртого всадника апокалипсиса.

