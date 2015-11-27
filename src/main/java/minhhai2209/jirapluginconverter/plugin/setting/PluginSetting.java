package minhhai2209.jirapluginconverter.plugin.setting;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.upm.api.license.PluginLicenseManager;

import minhhai2209.jirapluginconverter.connect.descriptor.Descriptor;
import minhhai2209.jirapluginconverter.connect.descriptor.Modules;
import minhhai2209.jirapluginconverter.utils.ExceptionUtils;
import minhhai2209.jirapluginconverter.utils.JsonUtils;

public class PluginSetting {

  public static final String GROUP_ID = "generated_group_id";

  public static final String ARTIFACT_ID = "generated_artifact_id";

  public static final String PLUGIN_KEY = GROUP_ID + "." + ARTIFACT_ID;

  public static final String URL_SAFE_PLUGIN_KEY = GROUP_ID + "-" + ARTIFACT_ID;

  private static Descriptor descriptor;

  public static void load(
      PluginSettingsFactory pluginSettingsFactory,
      TransactionTemplate transactionTemplate,
      PluginLicenseManager pluginLicenseManager) throws Exception {
    readDescriptor();
    KeyUtils.loadJiraConsumer();
    KeyUtils.generateSharedSecret(pluginSettingsFactory, transactionTemplate);
  }

  private static void readDescriptor() {
    InputStream is = null;
    try {
      is = PluginSetting.class.getResourceAsStream("/imported_atlas_connect_descriptor.json");
      String descriptorString = IOUtils.toString(is);
      descriptor = JsonUtils.fromJson(descriptorString, Descriptor.class);
      WebItemUtils.buildWebItemLookup();
      WebPanelUtils.buildWebPanelLookup();
      PageUtils.buildGeneralPageLookup();
      PageUtils.buildAdminPageLookup();
    } catch (Exception e1) {
      if (is != null) {
        try {
          is.close();
        } catch (Exception e2) {
          ExceptionUtils.throwUnchecked(e2);
        }
      }
      ExceptionUtils.throwUnchecked(e1);
    }
  }

  public static Descriptor getDescriptor() {
    return descriptor;
  }

  public static String getJiraBaseUrl() {
    return ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL);
  }

  public static String getPluginBaseUrl() {
    return descriptor.getBaseUrl();
  }

  public static Modules getModules() {
    return descriptor.getModules();
  }
}
