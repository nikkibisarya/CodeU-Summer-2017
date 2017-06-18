package codeu.chat.server;

import java.util.concurrent.BlockingQueue;
import java.lang.Runnable;

import codeu.chat.common.Writeable;
import codeu.chat.util.Serializers;

import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import codeu.chat.common.User;

import codeu.chat.util.Logger;

public class fileWriter implements Runnable {

    private static final Logger.Log LOG = Logger.newLog(fileWriter.class);


    private static BlockingQueue<Writeable> queue;
    private static final String TRANSACTION_FILE = "transaction.log";

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
      }

      Logger.enableConsoleOutput();

      // type like: "user"
      try {
        //LOG.error(x.getType());
        //LOG.error(x instanceof User ? "yes" : "no");
        //LOG.error(x.getType());
        Serializers.STRING.write(fout, "user");
        x.write(fout, x);
        fout.close();
      } catch (IOException e) {
        System.err.println("couldn't write to transaction log file");
      }
   }
}
