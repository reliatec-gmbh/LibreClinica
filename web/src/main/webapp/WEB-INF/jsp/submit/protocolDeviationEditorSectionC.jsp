<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<div class="section-c" style="display: flex; flex-direction: column">
    <h2>C. Incident Description</h2>
    <ol>
        <li>
            <span class="question">What is the reason for the protocol deviation (check all that apply)?</span>
            <div class="detail vertical">
                <label><input type="checkbox" name="item_c_1_1" value="1"> Participant illness</label>
                <label><input type="checkbox" name="item_c_1_2" value="1"> Participant unable to comply</label>
                <label><input type="checkbox" name="item_c_1_3" value="1"> Participant refusal</label>
                <label><input type="checkbox" name="item_c_1_4" value="1"> Study staff error</label>
                <label><input type="checkbox" name="item_c_1_5" value="1"> Pharmacist error</label>
                <label><input type="checkbox" name="item_c_1_6" value="1"> Laboratorian error</label>
                <label><input type="checkbox" name="item_c_1_7" value="1"> Investigator/study decision</label>
                <label><input type="checkbox" name="item_c_1_8" value="1"> Shipment error</label>
                <label><input type="checkbox" name="item_c_1_9" value="1"> Other, specify</label>
                <label><input type="text" name="item_c_1_10"></label>
            </div>
        </li>
        <li>
            <span class="question">Provide a detailed description of the incident:</span>
            <div class="detail">
                <textarea name="item_c_2"></textarea>
            </div>
        </li>
    </ol>
</div>