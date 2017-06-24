package codeu.chat.server;

import java.util.concurrent.BlockingQueue;
import java.lang.Runnable;

import codeu.chat.common.Writeable;
import codeu.chat.util.Serializers;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import codeu.chat.common.User;

import codeu.chat.util.Logger;

public class fileWriter implements Runnable {

    private static BlockingQueue<Writeable> queue;
    public static final String TRANSACTION_FILE = "transaction.log";

    public static final String CNT_FILE = "transaction.cnt";

    public fileWriter(BlockingQueue<Writeable> q) {
      queue = q;
    }

    public void run() {
      try {
        while (true) {
          write(queue.take());
        }
      } catch (InterruptedException ex) {

      }
    }

    public static void insert(Writeable x) throws InterruptedException {
      queue.put(x);
    }

    public void write(Writeable x) {

      File file = new File(TRANSACTION_FILE);

      FileOutputStream fout = null;
      try {
        fout = new FileOutputStream(file, true);
      } catch (FileNotFoundException e) {
        System.err.println("couldn't find transaction log file");
      } catch (SecurityException e) {
        System.err.println("can't access write to transaction log file");
      }


      // type like: "user"
      File countFile = new File(CNT_FILE);
      boolean exists = countFile.exists();

      FileOutputStream countfileOut = null;
      try {

        if(!exists) {
          countfileOut = new FileOutputStream(countFile, false);
          String str = "1";
          byte[] b = str.getBytes();
          countfileOut.write(str.getBytes());
          countfileOut.close();
        } else {
          FileInputStream countfileIn = new FileInputStream(countFile);
          byte[] bytes = new byte[(int)(countFile.length())];
          countfileIn.read(bytes);
          String oldCountString = new String(bytes);
          String newCountString = (Integer.parseInt(oldCountString) + 1) + "";
          countfileOut = new FileOutputStream(countFile, false);
          countfileOut.write(newCountString.getBytes());
          countfileOut.close();
          countfileIn.close();
        }
      } catch (FileNotFoundException e) {
        System.err.println("couldn't find count log file");
      } catch (SecurityException e) {
        System.err.println("can't access write to count file");
      } catch (IOException e) {

      }

      try {
        Serializers.STRING.write(fout, x.getType());
        x.write(fout, x);
        fout.close();
      } catch (IOException e) {
        System.err.println("couldn't write to transaction log file");
      }

   }
}
