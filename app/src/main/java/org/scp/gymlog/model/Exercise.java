package org.scp.gymlog.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.scp.gymlog.util.json.JsonMapped;
import org.scp.gymlog.util.json.JsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter
public class Exercise implements JsonMapped {
	private final List<MuscularGroup> belongingMuscularGroups = new ArrayList<>();
	private String name;
	private String image;

	@Override
	public JSONObject toJson() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("name", name);
		json.put("image", image);

		JSONArray groups = belongingMuscularGroups.stream()
				.map(MuscularGroup::getId)
				.collect(JsonUtils.collector());

		json.put("groups", groups);
		return json;
	}

	@Override
	public void fromJson(JSONObject json) throws JSONException {
		this.name = json.getString("name");
		this.image = json.getString("image");
		this.belongingMuscularGroups.clear();

		List<MuscularGroup> muscularGroups = Data.getInstance().getMuscularGroups();
		JSONArray groups = json.optJSONArray("groups");
		JsonUtils.forEachInt(groups, id -> {
			for (MuscularGroup muscularGroup : muscularGroups) {
				if (muscularGroup.getId() == id) {
					belongingMuscularGroups.add(muscularGroup);
					break;
				}
			}
		});
	}
}
