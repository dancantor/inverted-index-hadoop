package hpc;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.conf.Configuration;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashSet;
import java.util.StringTokenizer;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;


public class InvertedIndexMapper extends Mapper<LongWritable, Text, Text, Text> {
    private Text word = new Text();
    private static final Text location = new Text();
    private HashSet<String> stopWords = new HashSet<>();

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        URI[] cacheFiles = Job.getInstance(context.getConfiguration()).getCacheFiles();
        if (cacheFiles != null && cacheFiles.length > 0) {
            readFile(new Path(cacheFiles[0]));
        }
    }

    private void readFile(Path filePath) throws IOException {
        FileSystem fs = FileSystem.get(new Configuration());
        BufferedReader reader = new BufferedReader(new InputStreamReader(fs.open(filePath)));
        String line;
        while ((line = reader.readLine()) != null) {
            stopWords.add(line.trim().toLowerCase());
        }
    }

    @Override
    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        FileSplit fileSplit = (FileSplit) context.getInputSplit();
        String fileName = fileSplit.getPath().getName();
        StringTokenizer itr = new StringTokenizer(value.toString());

        while (itr.hasMoreTokens()) {
            String currentWord = itr.nextToken().toLowerCase();
            if (!stopWords.contains(currentWord)) {
                word.set(currentWord);
                location.set(fileName + "@" + key.toString());
                context.write(word, location);
            }
        }
    }
}
