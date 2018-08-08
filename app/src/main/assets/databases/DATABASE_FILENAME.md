There is discrepency beween the name of the DB file used by the process that downloads the DB updates and the process that expands the DB that is shipped with the APK.


When shipping a database with an APK you must have the zip file have a file name like:
SEPTA_<VERSION>.sqlite.zip

The database that gets downloaded will have a format of the name:
SEPTA_<VERSION>_sqlite.zip