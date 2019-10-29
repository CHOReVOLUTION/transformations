package ${bcData.packagename}.business;

<#if isOutputRootMultiple>
import java.util.List;
</#if>

import ${bcData.packagename}.${inputRootElement?cap_first};
import ${bcData.packagename}.${outputRootElement?cap_first};

public interface ${bcData.servicename?cap_first}Service {

	<#if isOutputRootMultiple>List<</#if>${outputRootElement?cap_first}<#if isOutputRootMultiple>></#if> ${operationName?uncap_first}(${inputRootElement?cap_first} params) throws BusinessException;
}
