package cc.tonyhook.berry.backend.web;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

public class BerryCachedBodyHttpServletResponse extends HttpServletResponseWrapper {

    private BerryCachedBodyServletOutputStream cachedBody;

    public BerryCachedBodyHttpServletResponse(HttpServletResponse response) throws IOException {
        super(response);
        cachedBody = new BerryCachedBodyServletOutputStream(response.getOutputStream());
    }

    public byte[] getContentAsByteArray() throws IOException {
        cachedBody.flush();
        return cachedBody.getOutputStream().toString("UTF-8").getBytes();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return cachedBody;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(cachedBody);
    }

}
