package minhhai2209.jirapluginconverter.plugin.render;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.text.StrSubstitutor;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;

public class ParameterContextBuilder {

  @SuppressWarnings("unchecked")
  private static void buildContextParams(HttpServletRequest request, Map<String, String> acContext) {

    acContext.put("issue.id", "");
    acContext.put("issue.key", "");
    acContext.put("issuetype.id", "");
    acContext.put("project.id", "");
    acContext.put("project.key", "");
    acContext.put("version.id", "");
    acContext.put("component.id", "");

    Map<String, String[]> contextParams = request.getParameterMap();
    ProjectManager projectManager = ComponentAccessor.getProjectManager();
    IssueManager issueManager = ComponentAccessor.getIssueManager();

    if (contextParams.containsKey("issueKey")) {
      String issueKey = contextParams.get("issueKey")[0];
      if (!issueKey.contains("$")) {
        acContext.put("issue.key", issueKey);
        MutableIssue issue = issueManager.getIssueByCurrentKey(issueKey);
        if (issue != null) {
          acContext.put("issue.id", issue.getId().toString());
          acContext.put("issuetype.id", issue.getIssueTypeId());
        }
      }
    }

    if (contextParams.containsKey("projectKey")) {
      String projectKey = contextParams.get("projectKey")[0];
      if (!projectKey.contains("$")) {
        acContext.put("project.key", projectKey);
        Project project = projectManager.getProjectByCurrentKey(projectKey);
        if (project != null) {
          acContext.put("project.id", project.getId().toString());
        }
      }
    }

    if (contextParams.containsKey("versionId")) {
      String versionId = contextParams.get("versionId")[0];
      if (!versionId.contains("$")) {
        acContext.put("version.id", versionId);
      }
    }

    if (contextParams.containsKey("componentId")) {
      String componentId = contextParams.get("componentId")[0];
      if (!componentId.contains("$")) {
        acContext.put("component.id", componentId);
      }
    }
  }

  private static void buildContextParams(Map<String, Object> contextParams, Map<String, String> acContext) {

    Object o = contextParams.get("issue");
    if (o instanceof Issue) {
      Issue issue = (Issue) o;
      if (issue != null) {
        acContext.put("issue.key", issue.getKey());
        acContext.put("issue.id", issue.getId().toString());
        acContext.put("issuetype.id", issue.getIssueTypeId());
      }
    }

    o = contextParams.get("project");
    if (o instanceof Project) {
      Project project = (Project) o;
      if (project != null) {
        acContext.put("project.key", project.getKey());
        acContext.put("project.id", project.getId().toString());
      }
    }
  }

  private static void buildContextParams(Issue issue, Map<String, String> acContext) {

    if (issue != null) {
      acContext.put("issue.key", issue.getKey());
      acContext.put("issue.id", issue.getId().toString());
      acContext.put("issuetype.id", issue.getIssueTypeId());
    }
  }

  public static String buildUrl(String url, Map<String, String> acContext) {
    StrSubstitutor substitutor = new StrSubstitutor(acContext, "${", "}");
    url = substitutor.replace(url);
    return url;
  }

  public static Map<String, String> buildContext(
      HttpServletRequest request, Map<String, Object> contextParams, Issue issue) {
    Map<String, String> acContext = new HashMap<String, String>();
    if (request != null) {
      buildContextParams(request, acContext);
    } else if (contextParams != null) {
      buildContextParams(contextParams, acContext);
    } else if (issue != null) {
      buildContextParams(issue, acContext);
    }
    return acContext;
  }
}
