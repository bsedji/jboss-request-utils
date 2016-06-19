package com.yama.javax.servlet;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.jboss.logging.Logger;

public class RequestLoggerValve
    extends ValveBase
{
  private static Logger log = Logger.getLogger(RequestLoggerValve.class);

  private static final String CR = "\n";

  public void invoke(Request request, Response response) throws IOException, ServletException {
    if (log.isInfoEnabled()) {
      StringBuilder preInvokeLog = new StringBuilder("");
      StringBuilder postInvokeLog = new StringBuilder("");
      String head = "########################################";
      String uuid = UUID.randomUUID().toString();
      try {
        request.setInputStream(new BufferedServletInputStream(request));
        response.setOutputStream(new BufferedServletOutputStream(response));
        preInvokeLog.append(head);
        preInvokeLog.append(CR);
        preInvokeLog.append("request uuid=" + uuid);
        preInvokeLog.append(CR);
        preInvokeLog.append(request.getMethod() + " " + request.getRequestURI());
        preInvokeLog.append(CR);
        preInvokeLog.append(head);
        preInvokeLog.append(CR);
        log.info(preInvokeLog);
      }
      catch (Exception exception) {
        // no op
        log.error("Exception when building preInvokeLog", exception);
      }
      getNext().invoke(request, response);
      try {
        postInvokeLog.append(head);
        postInvokeLog.append(CR);
        postInvokeLog.append("response uuid=" + uuid);
        postInvokeLog.append(CR);
        buildRequest(request, response, postInvokeLog);
        buildResponse(request, response, postInvokeLog);
        postInvokeLog.append(head);
        log.info(postInvokeLog);
      }
      catch(Exception exception) {
        // no op
        log.error("Exception when building postInvokeLog", exception);
      }
    }
    else {
      getNext().invoke(request, response);
    }
  }

  private void buildRequest(Request request, Response response, StringBuilder stringBuilder) throws IOException {
    stringBuilder.append("request body:");
    stringBuilder.append(CR);
    stringBuilder.append(new String(((BufferedServletInputStream) request.getInputStream()).getInputBuffer()));
    stringBuilder.append(CR);
  }

  private void buildResponse(Request request, Response response, StringBuilder stringBuilder) throws IOException {
    stringBuilder.append("response body:");
    stringBuilder.append(CR);
    stringBuilder
        .append(new String(((BufferedServletOutputStream) response.getOutputStream()).getOutputStreamAsByteArray()));
    stringBuilder.append(CR);
  }
}
