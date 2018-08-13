package com.wacai.open.sdk.filter;

import com.wacai.open.sdk.util.RequestSignUtil;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WacaiOpenSignCheckerFilter implements Filter {

	private static final Logger log = LoggerFactory.getLogger(WacaiOpenSignCheckerFilter.class);

	private static final byte[] sign_not_match_error = "{\"code\": \"10008\", \"error\": \"sign值不匹配\"}"
			.getBytes(Charset.forName("UTF-8"));

	/**
	 * 是否开启 sign 校验
	 */
	private boolean enableSignCheck = true;

	/**
	 * 对应的 appSecret
	 */
	private String appSecret;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		String enableSignCheck = filterConfig.getInitParameter("enableSignCheck");
		if (enableSignCheck != null) {
			this.enableSignCheck = Boolean.valueOf(enableSignCheck);
		}

		String appSecret = filterConfig.getInitParameter("appSecret");
		if (appSecret == null || appSecret.trim().length() <= 0) {
			throw new ServletException("invalid app secret " + appSecret);
		}
		this.appSecret = appSecret;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		if (!enableSignCheck) {
			chain.doFilter(request, response);
			return;
		}

		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		httpServletRequest = new WacaiHttpServletRequest(httpServletRequest);
		boolean isSignMatch = RequestSignUtil.checkInboundRequestSign(httpServletRequest, appSecret);
		if (!isSignMatch) {
			log.error("sign is not match, request path is {}", httpServletRequest.getPathInfo());

			sendErrorResponse((HttpServletResponse) response);
			return;
		}

		chain.doFilter(httpServletRequest, response);
	}

	private void sendErrorResponse(HttpServletResponse httpServletResponse) throws IOException {
		httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		httpServletResponse.setContentType("application/json");
		httpServletResponse.setCharacterEncoding("UTF-8");

		httpServletResponse.getOutputStream().write(sign_not_match_error);
		httpServletResponse.flushBuffer();
	}

	@Override
	public void destroy() {
	}

}
