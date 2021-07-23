import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class Hw3 {


    // TASK 1:

    public static class TokenizerMapper1
            extends Mapper<Object, Text, Text, IntWritable> {


        private final Text word = new Text();

        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {
            StringTokenizer itr = new StringTokenizer(value.toString());

            int count = 0;

            while (itr.hasMoreTokens()) {


                count++;

                String token = itr.nextToken();

                if ((count % 3) == 0) {
                    word.set("duration");
                    IntWritable duration = new IntWritable(Integer.parseInt(token));
                    context.write(word, duration);
                    count = 0;
                }
            }

        }
    }

    public static class IntSumReducer1
            extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();
        private final Text word = new Text();

        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            word.set("total_count");
            context.write(word, result);
        }
    }

    // --------------------------------------------------------------

    // TASK 2:

    public static class TokenizerMapper2
            extends Mapper<Object, Text, Text, IntWritable> {

        private final static IntWritable one = new IntWritable(1);
        private final Text word = new Text();

        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {
            StringTokenizer itr = new StringTokenizer(value.toString());

            int count = 0;

            while (itr.hasMoreTokens()) {

                count++;

                String token = itr.nextToken();

                if ((count % 3) != 0) {
                    word.set(token);
                    context.write(word, one);

                } else {
                    count = 0;
                }
            }

        }
    }

    public static class IntSumReducer2
            extends Reducer<Text, IntWritable, Text, IntWritable> {

        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }


    // --------------------------------------------------------------

    // TASK 3:

    public static class TokenizerMapper3
            extends Mapper<Object, Text, Text, DoubleWritable> {

        //private final static IntWritable one = new IntWritable(1);
        private final Text word = new Text();

        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {
            StringTokenizer itr = new StringTokenizer(value.toString());

            int count = 0;

            List<String> cities = new ArrayList<>();

            while (itr.hasMoreTokens()) {

                count++;

                String token = itr.nextToken();

                if ((count % 3) == 0) {

                    Collections.sort(cities);

                    word.set("" + cities.get(0) + "-" + cities.get(1));
                    DoubleWritable duration = new DoubleWritable(Double.parseDouble(token));

                    context.write(word, duration);
                    cities.clear();
                    count = 0;
                } else {
                    cities.add(token);
                }

            }

        }
    }

    public static class DoubleAvgReducer3
            extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {

        private DoubleWritable result = new DoubleWritable();

        public void reduce(Text key, Iterable<DoubleWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            double sum = 0;
            int count = 0;

            for (DoubleWritable val : values) {
                sum += val.get();
                count++;
            }
            result.set(sum/count);
            context.write(key, result);
        }
    }



    // --------------------------------------------------------------

    // TASK 4:

    public static class TokenizerMapper4
            extends Mapper<Object, Text, Text, IntWritable> {

        private final Text wordKey = new Text();

        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {
            StringTokenizer itr = new StringTokenizer(value.toString());

            int count = 0;
            List<String> cities = new ArrayList<>();

            while (itr.hasMoreTokens()) {

                count++;
                String token = itr.nextToken();

                if ((count % 3) == 0) {

                    Collections.sort(cities);

                    wordKey.set("" + cities.get(0) + "-" + cities.get(1));
                    IntWritable duration = new IntWritable(Integer.parseInt(token));

                    context.write(wordKey, duration);
                    cities.clear();
                    count = 0;
                } else {
                    cities.add(token);
                }

            }

        }
    }



    public static class Task4Partitioner extends
            Partitioner<Text, IntWritable> {

        public int getPartition(Text key, IntWritable value, int numReduceTasks) {

            int duration = value.get();

            if(numReduceTasks == 0) {
                return 0;
            }
            if (duration <= 5) {
                return 0;
            }
            else if((duration > 5) && (duration <= 10)) {
                return 1;
            }
            else if ((duration > 10) && (duration <= 15)) {
                return 2;
            }
            else {
                return 3;
            }
        }

    }


    public static class IntSumReducer4
            extends Reducer<Text, IntWritable, Text, IntWritable> {

        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            int sum = 0;

            for (IntWritable val : values) {
                sum++;
            }
            result.set(sum);
            context.write(key, result);
        }
    }



    public static void main(String[] args) throws Exception {

        if (args[0].equals("tot")) {
            Configuration conf = new Configuration();
            Job job = Job.getInstance(conf, "tot");
            job.setJarByClass(Hw3.class);
            job.setMapperClass(TokenizerMapper1.class);
            job.setCombinerClass(IntSumReducer1.class);
            job.setReducerClass(IntSumReducer1.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(IntWritable.class);
            FileInputFormat.addInputPath(job, new Path(args[1]));
            FileOutputFormat.setOutputPath(job, new Path(args[2]));
            System.exit(job.waitForCompletion(true) ? 0 : 1);
        }

        else if (args[0].equals("city")) {
            Configuration conf = new Configuration();
            Job job = Job.getInstance(conf, "city");
            job.setJarByClass(Hw3.class);
            job.setMapperClass(TokenizerMapper2.class);
            job.setCombinerClass(IntSumReducer2.class);
            job.setReducerClass(IntSumReducer2.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(IntWritable.class);
            FileInputFormat.addInputPath(job, new Path(args[1]));
            FileOutputFormat.setOutputPath(job, new Path(args[2]));
            System.exit(job.waitForCompletion(true) ? 0 : 1);
        }

        else if(args[0].equals("avg")) {
            Configuration conf = new Configuration();
            Job job = Job.getInstance(conf, "avg");
            job.setJarByClass(Hw3.class);
            job.setMapperClass(TokenizerMapper3.class);
            job.setCombinerClass(DoubleAvgReducer3.class);
            job.setReducerClass(DoubleAvgReducer3.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(DoubleWritable.class);
            FileInputFormat.addInputPath(job, new Path(args[1]));
            FileOutputFormat.setOutputPath(job, new Path(args[2]));
            System.exit(job.waitForCompletion(true) ? 0 : 1);
        }

        else if(args[0].equals("sep")) {
            Configuration conf = new Configuration();
            Job job = Job.getInstance(conf, "sep");
            job.setJarByClass(Hw3.class);
            job.setMapperClass(TokenizerMapper4.class);
            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(IntWritable.class);


            job.setPartitionerClass(Task4Partitioner.class);
            job.setReducerClass(IntSumReducer4.class);
            job.setNumReduceTasks(4);

            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(IntWritable.class);
            FileInputFormat.addInputPath(job, new Path(args[1]));
            FileOutputFormat.setOutputPath(job, new Path(args[2]));
            System.exit(job.waitForCompletion(true) ? 0 : 1);
        }

    }
}
