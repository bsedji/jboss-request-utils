package com.yama.javax.servlet;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.UUID;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.jboss.logging.Logger;

public class RequestLoggerValve
	 extends ValveBase
{
	private static Logger log = Logger.getLogger(RequestLoggerValve.class);

	private static final String CR = "\n";

	private static final String HEAD = "########################################";
	

	public void invoke(Request request, Response response) throws IOException, ServletException {
		if (log.isInfoEnabled()) {
			final String uuid = UUID.randomUUID().toString();
			try {
				request.setInputStream(new BufferedServletInputStream(request));
				response.setOutputStream(new BufferedServletOutputStream(response));
				// for setting usingOutputstream to false
				response.reset();
				logRequestOnly(request, response, uuid);
			}
			catch (Exception exception) {
				// no op
				log.error("Exception when building preInvokeLog", exception);
			}
			// invoke next handler
			getNext().invoke(request, response);
			try {
				if (request.getAsyncContext() == null) {
					logRequestResponse(request, response, uuid);
				}
				else {
					AsyncContext context = request.getAsyncContext();
					context.addListener(new AbstractAyncListener(container.getLogger())
					{
						@Override
						public void onComplete(final AsyncEvent asyncEvent) throws IOException {
							HttpServletResponse response = (HttpServletResponse) asyncEvent.getAsyncContext().getResponse();
							HttpServletRequest request = (HttpServletRequest) asyncEvent.getAsyncContext().getRequest();
							logRequestResponse(request, response, uuid);
						}
					});
				}
			}
			catch (Exception exception) {
				// no op
				log.error("Exception when building postInvokeLog", exception);
			}
		}
		else {
			getNext().invoke(request, response);
		}
	}

	private void logRequestOnly(final HttpServletRequest request, final HttpServletResponse response, String uuid) {

		StringBuilder preInvokeLog = new StringBuilder("");
		preInvokeLog.append(HEAD);
		preInvokeLog.append(CR);
		preInvokeLog.append("RequestLoggerValve request uuid=" + uuid);
		preInvokeLog.append(CR);
		preInvokeLog.append(request.getMethod() + " " + request.getRequestURI());
		preInvokeLog.append(CR);
		preInvokeLog.append("queryString=" + request.getQueryString());
		preInvokeLog.append(CR);
		preInvokeLog.append("headers:");
		preInvokeLog.append(CR);
		Enumeration hnames = request.getHeaderNames();
		while (hnames.hasMoreElements()) {
			String hname = (String) hnames.nextElement();
			Enumeration hvalues = request.getHeaders(hname);
			while (hvalues.hasMoreElements()) {
				String hvalue = (String) hvalues.nextElement();
				preInvokeLog.append(hname + "=" + hvalue);
				preInvokeLog.append(CR);
			}
		}
		preInvokeLog.append(HEAD);
		preInvokeLog.append(CR);
		log.info(preInvokeLog);
	}

	private void logRequestResponse(final HttpServletRequest request, final HttpServletResponse response, String uuid)
		 throws IOException
	{
		StringBuilder postInvokeLog = new StringBuilder("");
		postInvokeLog.append(HEAD);
		postInvokeLog.append(CR);
		postInvokeLog.append("RequestLoggerValve response uuid=" + uuid);
		postInvokeLog.append(CR);
		postInvokeLog.append("status=" + response.getStatus());
		postInvokeLog.append(CR);
		postInvokeLog.append("contentType=" + response.getContentType());
		postInvokeLog.append(CR);
		Collection<String> rhnames = response.getHeaderNames();
		for (String rhname : rhnames) {
			postInvokeLog.append(rhname + "=" + response.getHeader(rhname));
			postInvokeLog.append(CR);
		}
		buildRequest(request, response, postInvokeLog);
		buildResponse(request, response, postInvokeLog);
		postInvokeLog.append(HEAD);
		log.info(postInvokeLog);
	}

	private void buildRequest(HttpServletRequest request, HttpServletResponse response, StringBuilder stringBuilder)
		 throws IOException
	{
		stringBuilder.append("request body:");
		stringBuilder.append(CR);
		stringBuilder.append(new String(((BufferedServletInputStream) request.getInputStream()).getInputBuffer()));
		stringBuilder.append(CR);
	}

	private void buildResponse(HttpServletRequest request, HttpServletResponse response, StringBuilder stringBuilder)
		 throws IOException
	{
		stringBuilder.append("response body:");
		stringBuilder.append(CR);
		stringBuilder
			 .append(new String(((BufferedServletOutputStream) response.getOutputStream()).getOutputStreamAsByteArray()));
		stringBuilder.append(CR);
	}

	public abstract class AbstractAyncListener
		 implements AsyncListener
	{
		private Logger log;

		public AbstractAyncListener(Logger logger) {
			this.log = logger;
		}

		@Override
		public void onTimeout(final AsyncEvent asyncEvent) throws IOException {
		}

		@Override
		public void onError(final AsyncEvent asyncEvent) throws IOException {
		}

		@Override
		public void onStartAsync(final AsyncEvent asyncEvent) throws IOException {
		}
	}

}
