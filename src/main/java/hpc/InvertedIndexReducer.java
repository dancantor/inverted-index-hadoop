package hpc;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;

import java.io.IOException;
import java.util.*;

public class InvertedIndexReducer extends Reducer<Text, Text, Text, Text> {
    @Override
    public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        StringBuilder stringBuilder = new StringBuilder();
        HashMap<String, String> locations = new HashMap<>();
        for (Text val : values) {
            String bookName = val.toString().split("@")[0];
            String line = val.toString().split("@")[1];
            locations.put(bookName, locations.getOrDefault(bookName, "") + "|" + line);
        }

        for (Map.Entry<String, String> location : locations.entrySet()) {
            stringBuilder.append(location.getKey());
            stringBuilder.append("@");
            stringBuilder.append(location.getValue());
            stringBuilder.append("  ;  ");
        }

        context.write(key, new Text(stringBuilder.toString()));
    }
}
