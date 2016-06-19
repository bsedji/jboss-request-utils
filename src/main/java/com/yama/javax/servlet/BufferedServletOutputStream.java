package com.yama.javax.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;

/**
 * Copied from https://github.com/qos-ch/logback@TeeOutputStream
 */
public class BufferedServletOutputStream extends ServletOutputStream
{

  final ServletOutputStream underlyingStream;
  final ByteArrayOutputStream baosCopy;

  BufferedServletOutputStream(ServletResponse httpServletResponse) throws IOException {
    // System.out.println("TeeServletOutputStream.constructor() called");
    this.underlyingStream = httpServletResponse.getOutputStream();
    baosCopy = new ByteArrayOutputStream();
  }

  byte[] getOutputStreamAsByteArray() {
    return baosCopy.toByteArray();
  }

  @Override
  public void write(int val) throws IOException {
    if (underlyingStream != null) {
      underlyingStream.write(val);
      baosCopy.write(val);
    }
  }

  @Override
  public void write(byte[] byteArray) throws IOException {
    if (underlyingStream == null) {
      return;
    }
    write(byteArray, 0, byteArray.length);
  }

  @Override
  public void write(byte byteArray[], int offset, int length) throws IOException {
    if (underlyingStream == null) {
      return;
    }
    underlyingStream.write(byteArray, offset, length);
    baosCopy.write(byteArray, offset, length);
  }

  @Override
  public void close() throws IOException {
    // If the servlet accessing the stream is using a writer instead of
    // an OutputStream, it will probably call os.close() before calling
    // writer.close. Thus, the underlying output stream will be called
    // before the data sent to the writer could be flushed.
  }

  @Override
  public void flush() throws IOException {
    if (underlyingStream == null) {
      return;
    }
    underlyingStream.flush();
    baosCopy.flush();
  }
}
