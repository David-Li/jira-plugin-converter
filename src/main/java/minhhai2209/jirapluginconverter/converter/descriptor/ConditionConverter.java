package minhhai2209.jirapluginconverter.converter.descriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import minhhai2209.jirapluginconverter.plugin.descriptor.Condition;
import minhhai2209.jirapluginconverter.plugin.descriptor.Param;

public class ConditionConverter extends Converter<Condition, minhhai2209.jirapluginconverter.connect.descriptor.condition.Condition>{

  @Override
  public Condition toPluginModule(minhhai2209.jirapluginconverter.connect.descriptor.condition.Condition connectCondition) {
    Condition conditionModule = new Condition();
    conditionModule.setInvert(connectCondition.isInvert());
    List<Param> clauses = new ArrayList<Param>();
    clauses.add(new Param("condition", connectCondition.getCondition()));

    Map<String, String> params = connectCondition.getParams();
    if (params != null) {
      for (String key : params.keySet()) {
        clauses.add(new Param(key, params.get(key)));
      }
    }
    conditionModule.setParams(clauses);
    return conditionModule;
  }
  
  public List<Condition> getConditionModules(List<minhhai2209.jirapluginconverter.connect.descriptor.condition.Condition> connectConditions) {
    List<Condition> conditions = new ArrayList<Condition>();
    for (minhhai2209.jirapluginconverter.connect.descriptor.condition.Condition connectCondition : connectConditions) {
      conditions.add(toPluginModule(connectCondition));
    }
    return conditions;
  }

}
