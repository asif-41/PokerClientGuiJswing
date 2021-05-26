package Objects;

import org.json.JSONObject;

public class TransactionNumber {

    int id;
    String type;
    String number;

    public TransactionNumber() {
        id = -1;
        type = "";
        number = "";
    }

    public TransactionNumber(JSONObject jsonObject) {

        id = jsonObject.getInt("id");
        type = jsonObject.getString("type");
        number = jsonObject.getString("number");
    }

    public TransactionNumber(int id, String type, String number) {
        this.id = id;
        this.type = type;
        this.number = number;
    }

    public JSONObject getJSON(){

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String toString2() {
        return "TransactionNumber id: " + id + " type: " + type + ", number= " + number ;
    }

    @Override
    public String toString() {
        return "TransactionNumber{" +
                "id='" + id + '\'' +
                "type='" + type + '\'' +
                ", number='" + number + '\'' +
                '}';
    }
}
