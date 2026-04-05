package org.example.lsw_proto2.core;

import org.example.lsw_proto2.battle.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface InputService extends BattleInputService, CampaignInputService {

    default List<String> parseInput(String input) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = Pattern.compile("\"([^\"]*)\"|(\\S+)").matcher(input);

        while (matcher.find()) {
            if (matcher.group(1) != null) tokens.add(matcher.group(1)); //text with quotes
            else tokens.add(matcher.group(2));
        }

        return tokens;
    }
}
