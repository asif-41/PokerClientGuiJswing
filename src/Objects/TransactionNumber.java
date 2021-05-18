package Objects;

import org.json.JSONObject;

public class TransactionNumber {

    String type;
    String number;

    public TransactionNumber() { }

    public TransactionNumber(JSONObject jsonObject) {

        type = jsonObject.getString("type");
        number = jsonObject.getString("number");
    }

    public TransactionNumber(String type, String number) {
        this.type = type;
        this.number = number;
    }

    public JSONObject getJSON(){

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", type);
        jsonObject.put("number", number);

        return jsonObject;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "TransactionNumber{" +
                "type='" + type + '\'' +
                ", number='" + number + '\'' +
                '}';
    }
}