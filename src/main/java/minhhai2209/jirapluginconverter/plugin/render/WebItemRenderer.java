package minhhai2209.jirapluginconverter.plugin.render;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.timezone.TimeZoneService;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.sal.api.message.LocaleResolver;
import com.atlassian.templaterenderer.TemplateRenderer;
import minhhai2209.jirapluginconverter.connect.descriptor.Context;
import minhhai2209.jirapluginconverter.connect.descriptor.webitem.WebItem;
import minhhai2209.jirapluginconverter.connect.descriptor.webitem.WebItemTarget;
import minhhai2209.jirapluginconverter.connect.descriptor.webitem.WebItemTarget.Type;
import minhhai2209.jirapluginconverter.plugin.iframe.HostConfig;
import minhhai2209.jirapluginconverter.plugin.jwt.JwtComposer;
import minhhai2209.jirapluginconverter.plugin.setting.*;
import minhhai2209.jirapluginconverter.plugin.utils.EnumUtils;
import minhhai2209.jirapluginconverter.plugin.utils.LocaleUtils;
import minhhai2209.jirapluginconverter.plugin.utils.RequestUtils;
import minhhai2209.jirapluginconverter.utils.ExceptionUtils;
import minhhai2209.jirapluginconverter.utils.JsonUtils;
import org.apache.http.client.utils.URIBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class WebItemRenderer extends HttpServlet {

  private static final long serialVersionUID = 6917800660560978125L;

  private static final String RESPONSE_CONTENT_TYPE = "text/html;charset=utf-8";

  private TemplateRenderer renderer;

  private TimeZoneService timeZoneService;

  private LocaleResolver localeResolver;

  public WebItemRenderer(
      TemplateRenderer renderer,
      TimeZoneService timeZoneService,
      LocaleResolver localeResolver) {

    this.renderer = renderer;
    this.timeZoneService = timeZoneService;
    this.localeResolver = localeResolver;
  }

  @Override
  public void doGet(
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    try {

      String moduleKey = RequestUtils.getModuleKey(request);
      WebItem webItem = WebItemUtils.getWebItem(moduleKey);
      String webItemUrl = webItem.getUrl();
      String fullUrl = WebItemUtils.getFullUrl(webItem);
      WebItemTarget target = webItem.getTarget();
      Type type = null;
      if (target != null) {
        type = target.getType();
      }
      if (type == null) {
        type = Type.page;
      }
      Context context = webItem.getContext();
      if (context == null) {
        context = Context.addon;
      }

      JiraAuthenticationContext authenticationContext = ComponentAccessor.getJiraAuthenticationContext();
      ApplicationUser user = authenticationContext != null ? authenticationContext.getUser() : null;
      JiraServiceContextImpl jiraServiceContext = new JiraServiceContextImpl(user);
      TimeZone timeZone = user == null ?
          timeZoneService.getDefaultTimeZoneInfo(jiraServiceContext).toTimeZone() :
          timeZoneService.getUserTimeZoneInfo(jiraServiceContext).toTimeZone();

      Map<String, String> productContext = ParameterContextBuilder.buildContext(request, null, null);

      String xdm_e = JiraUtils.getBaseUrl();
      String cp = JiraUtils.getContextPath();
      String ns = PluginSetting.URL_SAFE_PLUGIN_KEY + "__" + moduleKey;
      String xdm_c = "channel-" + ns;
      String dlg = EnumUtils.equals(type, Type.dialog) ? "1" : "";
      String simpleDlg = dlg;
      String general = "";
      String w = "100%";
      String h = "100%";
      String productCtx = JsonUtils.toJson(productContext);
      String timezone = timeZone.getID();
      String loc = LocaleUtils.getLocale(localeResolver);
      String userId = user != null ? user.getUsername() : "";
      String userKey = user != null ? user.getKey() : "";
      String lic = LicenseUtils.getLic();
      String cv = "";

      String urlWithContext = ParameterContextBuilder.buildUrl(fullUrl, productContext);

      URIBuilder uriBuilder = new URIBuilder(urlWithContext);
      if (EnumUtils.equals(type, Type.dialog) ||
          (EnumUtils.equals(type, Type.page)
          && EnumUtils.equals(context, Context.addon)
          && !(webItemUrl.startsWith("http://") || webItemUrl.startsWith("https://")))) {
        uriBuilder = uriBuilder.addParameter("tz", timezone)
            .addParameter("loc", loc)
            .addParameter("user_id", userId)
            .addParameter("user_key", userKey)
            .addParameter("xdm_e", xdm_e)
            .addParameter("xdm_c", xdm_c)
            .addParameter("cp", cp)
            .addParameter("lic", lic)
            .addParameter("cv", cv);
      }
      if (dlg.equals("1")) {
        uriBuilder.addParameter("dialog", dlg)
            .addParameter("simpleDialog", simpleDlg);
      }

      if (AuthenticationUtils.needsAuthentication()) {
        String jwt = JwtComposer.compose(
            KeyUtils.getClientKey(),
            KeyUtils.getSharedSecret(),
            "GET",
            uriBuilder,
            userKey,
            webItemUrl);
        uriBuilder.addParameter("jwt", jwt);
      }
      String url = uriBuilder.toString();

      if (EnumUtils.equals(type, Type.page)) {

        response.sendRedirect(url);

      } else {

        HostConfig hostConfig = new HostConfig();
        hostConfig.setNs(ns);
        hostConfig.setKey(PluginSetting.URL_SAFE_PLUGIN_KEY);
        hostConfig.setCp(cp);
        hostConfig.setUid(userId);
        hostConfig.setUkey(userKey);
        hostConfig.setDlg(dlg);
        hostConfig.setSimpleDlg(simpleDlg);
        hostConfig.setGeneral(general);
        hostConfig.setW(w);
        hostConfig.setH(h);
        hostConfig.setSrc(url);
        hostConfig.setProductCtx(productCtx);
        hostConfig.setTimeZone(timezone);

        String hostConfigJson = JsonUtils.toJson(hostConfig);

        Map<String, Object> viewContext = new HashMap<String, Object>();
        viewContext.put("hostConfigJson", hostConfigJson);
        viewContext.put("ns", ns);
        viewContext.put("plugin", PluginSetting.getPlugin());
        render("web-item", response, viewContext);
      }

    } catch (Exception e) {
      ExceptionUtils.throwUnchecked(e);
    }
  }

  private void render(String vm, HttpServletResponse response, Map<String, Object> context) throws IOException {
    response.setContentType(RESPONSE_CONTENT_TYPE);
    renderer.render("templates/" + vm + ".vm", context, response.getWriter());
  }
}
