package org.fusesource.lmdbjni;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;

public class DatabaseTest {
  static {
    Setup.setLmdbLibraryPath();
  }

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  Env env;
  Database db;
  byte[] data = new byte[] {1,2,3};

  @Before
  public void before() throws IOException {
    String path = tmp.newFolder().getCanonicalPath();
    env = new Env(path);
    db = env.openDatabase();
  }

  @After
  public void after() throws IOException {
    db.close();
    env.close();
  }

  @Test
  public void testDrop() {
    byte[] bytes = {1,2,3};
    db.put(bytes, bytes);
    byte[] value = db.get(bytes);
    assertArrayEquals(value, bytes);
    // empty
    db.drop(false);
    value = db.get(bytes);
    assertNull(value);
    db.put(bytes, bytes);
    db.drop(true);
    try {
      db.get(bytes);
      fail("db has been closed");
    } catch (LMDBException e) {

    }
  }

  @Test
  public void testStat() {
    db.put(new byte[]{1}, new byte[]{1});
    db.put(new byte[]{2}, new byte[]{1});
    Stat stat = db.stat();
    System.out.println(stat);
    assertThat(stat.getEntries(), is(2L));
    assertThat(stat.getPsize(), is(not(0L)));
    assertThat(stat.getOverflowPages(), is(0L));
    assertThat(stat.getDepth(), is(1L));
    assertThat(stat.getLeafPages(), is(1L));
  }

  @Test
  public void testDeleteBuffer() {
    db.put(new byte[]{1}, new byte[]{1});
    DirectBuffer key = new DirectBuffer(ByteBuffer.allocateDirect(1));
    key.putByte(0, (byte) 1);
    db.delete(key);
    assertNull(db.get(new byte[]{1}));
  }
}
