package minhhai2209.jirapluginconverter.converter;

import java.io.File;

import minhhai2209.jirapluginconverter.converter.utils.ConverterUtils;
import minhhai2209.jirapluginconverter.plugin.properties.PluginProperties;
import minhhai2209.jirapluginconverter.utils.ExceptionUtils;

public class Converter {

  public static void generate(String templatePath, PluginProperties info) {

    try {
      File root = ConverterUtils.getTemplate(templatePath);
      String connectFile = ConverterUtils.getConnectFile(info.getUrl());
      ConverterUtils.replaceTextInFolder(root, "generated_artifact_id", info.getArtifactId());
      ConverterUtils.replaceTextInFolder(root, "generated_group_id", info.getGroupId());
      ConverterUtils.replaceTextInFolder(root, "generated_company_name", info.getCompany());
      ConverterUtils.replaceTextInFolder(root, "generated_company_url", info.getCompanyUrl());
      ConverterUtils.replaceTextInFolder(root, "generated_description", info.getDescription());
      ConverterUtils.replaceTextInDescriptor(root, connectFile);
      ConverterUtils.copy(root, connectFile);
    } catch (Exception e) {
      ExceptionUtils.throwUnchecked(e);
    }
  }

  public static void main(String[] args) {

    PluginProperties info = new PluginProperties();
    info.setArtifactId(args[0]);
    info.setGroupId(args[1]);
    info.setCompany(args[2]);
    info.setCompanyUrl(args[3]);
    info.setDescription(args[4]);
    info.setUrl(args[5]);
    generate(args[6], info);
  }
}
