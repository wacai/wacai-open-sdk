package com.wacai.open.sdk.filter;

import com.wacai.open.sdk.errorcode.ErrorCode;
import com.wacai.open.sdk.json.JsonTool;
import com.wacai.open.sdk.response.WacaiErrorResponse;
import com.wacai.open.sdk.util.SignUtil;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WacaiOpenSignCheckerFilter implements Filter {

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
    boolean isSignMatch = SignUtil.checkInboundRequestSign(httpServletRequest, appSecret);
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

    WacaiErrorResponse wacaiErrorResponse = new WacaiErrorResponse();
    wacaiErrorResponse.setCode(ErrorCode.SIGN_NOT_MATCH.getCode());
    wacaiErrorResponse.setError(ErrorCode.SIGN_NOT_MATCH.getDescription());

    httpServletResponse.getOutputStream().write(JsonTool.serialization(wacaiErrorResponse));
    httpServletResponse.flushBuffer();
  }

  @Override
  public void destroy() {
  }

}
