package org.fusesource.lmdbjni;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.File;
import java.io.IOException;

import static org.fusesource.lmdbjni.Constants.APPEND;

public class Setup {

  public static File dir = new File("/tmp/test");
  public static Database database;
  public static Env env;
  public static JNI.MDB_val valueVal;
  public static JNI.MDB_val keyVal;

  public static void initLMDB() {
    setLmdbLibraryPath();
    valueVal = new JNI.MDB_val();
    keyVal = new JNI.MDB_val();
    recreateDir(dir);
    env = new Env();
    env.open(dir.getAbsolutePath());
    env.setMapSize(4_294_967_296L);
    database = env.openDatabase("test");
    Transaction tx = env.createTransaction();
    for (int i = 0; i < 100000; i++) {
      database.put(tx, Bytes.fromLong(i), Bytes.fromLong(i), APPEND);
    }
    tx.commit();
  }

  static RocksDB rocksDb;

  public static void initRocksDB() {
    recreateDir(dir);
    org.rocksdb.Options options = new org.rocksdb.Options();
    options.setCreateIfMissing(true);
    try {
      rocksDb = RocksDB.open(options, dir.getAbsolutePath());
      for (int i = 0; i < 100000; i++) {
        rocksDb.put(Bytes.fromLong(i), Bytes.fromLong(i));
      }
    } catch (RocksDBException e) {
      throw new RuntimeException(e);
    }
  }

  static DB leveldb;

  public static void initLevelDb() {
    recreateDir(dir);
    try {
      leveldb = Iq80DBFactory.factory.open(dir, new org.iq80.leveldb.Options());
      for (int i = 0; i < 100000; i++) {
        leveldb.put(Bytes.fromLong(i), Bytes.fromLong(i));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static String OS = System.getProperty("os.name").toLowerCase();

  public static boolean isWindows() {
    return (OS.indexOf("win") >= 0);
  }

  public static boolean isMac() {
    return (OS.indexOf("mac") >= 0);
  }

  public static void setLmdbLibraryPath() {
    File path = new File(".");
    if (isWindows()) {
      path = new File(path, "../lmdbjni-win64/target/native-build/target/lib");
    } else if (isMac()) {
      path = new File(path, "../lmdbjni-osx64/target/native-build/target/lib");
    } else {
      path = new File(path, "../lmdbjni-linux64/target/native-build/target/lib");
    }
    if (!path.exists()) {
      throw new IllegalStateException("Please build lmdbjni first " +
        "with a platform specific profile");
    }
    System.setProperty("library.lmdbjni.path", path.getAbsolutePath());
  }

  public static void recreateDir(File dir) {
    // delete one level.
    if (dir.isDirectory()) {
      File[] files = dir.listFiles();
      if (files != null)
        for (File file : files)
          if (file.isDirectory()) {
            recreateDir(file);
          } else {
            file.delete();
          }
    }
    dir.delete();
    dir.mkdirs();
  }
}
