package codeu.chat.util;

import java.io.IOException;
import java.lang.String;
import java.lang.Character;

public final class Tokenizer {

  private StringBuilder token;
  private String source;
  private int at;

  public Tokenizer(String source) {
    this.source = source;
    at = 0;
    token = new StringBuilder();
  }

  private int remaining() {
    return source.length() - at;
  }

  private char peek() throws IOException {
    if (at < source.length()) {
      return source.charAt(at);
    } else {
      throw new IOException();
    }
  }

  private char read() throws IOException {
    final char c = peek();
    at += 1;
    return c;
  }

  private String readWithNoQuotes() throws IOException {
    token.setLength(0);
    while (remaining() > 0 && !Character.isWhitespace(peek())){
      token.append(read());
    }
    return token.toString();
  }

  private String readWithQuotes() throws IOException {
    token.setLength(0);
    if (read() != '"') {
      throw new IOException("String must start with open quote");
    }
    while (peek() != '"') {
      token.append(read());
    }
    read();
    return token.toString();
  }

  public String next() throws IOException {
    while (remaining() > 0 && Character.isWhitespace(peek())){
      read();
    }
    if (remaining() <= 0) {
      return null;
    } else if (peek() == '"') {
      return readWithQuotes();
    } else {
      return readWithNoQuotes();
    }
  }
}
