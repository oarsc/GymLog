package org.scp.gymlog.util.json;

import org.json.JSONException;
import org.json.JSONObject;

public interface JsonMapped {
    JSONObject toJson() throws JSONException;
    void fromJson(JSONObject json) throws JSONException;
}
