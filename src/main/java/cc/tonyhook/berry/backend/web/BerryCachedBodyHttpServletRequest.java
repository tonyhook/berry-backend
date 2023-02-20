package cc.tonyhook.berry.backend.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.util.StreamUtils;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

public class BerryCachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    private byte[] cachedBody;

    public BerryCachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
    }

    public byte[] getContentAsByteArray() throws IOException {
        return cachedBody;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new BerryCachedBodyServletInputStream(this.cachedBody);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(new BerryCachedBodyServletInputStream(this.cachedBody)));
    }

}
