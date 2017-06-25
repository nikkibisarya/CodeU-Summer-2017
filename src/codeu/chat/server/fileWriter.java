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

// class to store current queued transactions and write to log file
public class FileWriter implements Runnable {

    private BlockingQueue<Writeable> queue;
    public static final String TRANSACTION_FILE = "transaction.log";

    public FileWriter(BlockingQueue<Writeable> q) {
      queue = q;
    }

    // function to constantly take from queue and write to file
    public void run() {
      try {
        while (true) {

          // use take() to wait for completed operations before taking from queue
          write(queue.take());
        }
      } catch (InterruptedException ex) {

      }
    }

    public void insert(Writeable x) throws InterruptedException {

      // adding to the common queue
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

      try {

        // writing the saved state into transaction file
        byte[] separator = new byte[1];
        separator[0] = 0x00;

        // write the separator to separate between saved objects
        fout.write(separator);
        Serializers.STRING.write(fout, x.getType());
        x.write(fout, x);
        fout.close();
      } catch (IOException e) {
        System.err.println("couldn't write to transaction log file");
      }

   }
}
