package ${generatingJavaData.modelPackageName};

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ChoreographyLoopIndexes {

	public Map<String, Integer> loopIndexes;

	public ChoreographyLoopIndexes(String choreographyLoopIndexes) {

		loopIndexes = Collections.synchronizedMap(new HashMap<>());
		String[] listLoopIndexes = choreographyLoopIndexes.split(",");
		for (String element : listLoopIndexes) {
			String[] s = element.split("#");
			if (s.length != 0 && !"".equals(s[0])) {
				try {
					loopIndexes.put(s[0], Integer.parseInt(s[1])-1);
				} catch (Exception e) {
					//Never Happen
				}
			}
		}
	}

	public int getChoreographyLoopIndex(String choreographyIndexName) {

		if (loopIndexes.containsKey(choreographyIndexName))
			return loopIndexes.get(choreographyIndexName).intValue();
		else
			return -1;
	}

}
