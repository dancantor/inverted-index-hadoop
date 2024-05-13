package hpc;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.util.LineReader;

import java.io.IOException;

public class LineRecordReader extends RecordReader<LongWritable, Text> {
    private LineReader in;
    private LongWritable key;
    private Text value = new Text();
    private long start = 0;
    private long end = 0;
    private long pos = 0;
    private int maxLineLength;

    @Override
    public void initialize(InputSplit genericSplit, TaskAttemptContext context) throws IOException {
        FileSplit split = (FileSplit) genericSplit;
        final Path file = split.getPath();
        Configuration conf = context.getConfiguration();
        this.maxLineLength = conf.getInt("mapreduce.input.linerecordreader.line.maxlength", Integer.MAX_VALUE);
        FileSystem fs = file.getFileSystem(conf);
        FSDataInputStream fileIn = fs.open(split.getPath());

        start = split.getStart();
        end = start + split.getLength();
        in = new LineReader(fileIn, conf);

        if (start != 0) {
            start += in.readLine(new Text(), 0, (int) Math.min(Integer.MAX_VALUE, end - start));
        }
        pos = start;
    }

    @Override
    public boolean nextKeyValue() throws IOException {
        if (key == null) {
            key = new LongWritable();
        }
        key.set(pos);

        if (value == null) {
            value = new Text();
        }
        int newSize = in.readLine(value, maxLineLength, Math.max((int) Math.min(Integer.MAX_VALUE, end - pos), maxLineLength));
        if (newSize == 0) {
            return false;
        }
        pos += newSize;
        if (newSize < maxLineLength) {
            pos += newSize;
        }
        return true;
    }

    @Override
    public LongWritable getCurrentKey() {
        return key;
    }

    @Override
    public Text getCurrentValue() {
        return value;
    }

    @Override
    public float getProgress() throws IOException {
        if (start == end) {
            return 0.0f;
        } else {
            return Math.min(1.0f, (pos - start) / (float) (end - start));
        }
    }

    @Override
    public void close() throws IOException {
        if (in != null) {
            in.close();
        }
    }
}