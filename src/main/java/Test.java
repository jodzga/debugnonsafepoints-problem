import com.linkedin.data.DataMap;
import com.linkedin.data.schema.RecordDataSchema;
import com.linkedin.data.template.DataTemplateUtil;
import com.linkedin.data.template.GetMode;
import com.linkedin.data.template.RecordTemplate;
import com.linkedin.data.template.SetMode;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;


public class Test {

  @State(Scope.Benchmark)
  public static class TestState {
    byte[] source = new byte[32*1024];
    byte[] target = new byte[32*1024];
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  public Foo test(TestState state) {

    someFunction(state);

    Foo foo = new Foo();
    Bar bar = new Bar();
    foo.setRecord(bar);
    bar.setInt(54);
    return foo;
  }

  public static void main(String[] args) throws Exception {
    new Test().runTest();
  }

  public void runTest() throws InterruptedException
  {
    TestState state = new TestState();
    for (int i= 0; i < Integer.MAX_VALUE; ++i)
    {
      test(state);
    }
  }

  void someFunction(TestState state) {
    arraycopy(state.source, 0, state.target, 0, state.source.length);
  }

  public void arraycopy(byte[] source, int srcPos, byte[] target, int destPos, int length) {
    for (int i = 0; i < length; i++) {
      target[destPos + i] = source[srcPos + i];
    }
  }

  public static class Foo extends RecordTemplate
  {
    public static final RecordDataSchema SCHEMA = (RecordDataSchema) DataTemplateUtil.parseSchema(
        "{ \"type\" : \"record\", \"name\" : \"Foo\", \"fields\" : [\n" +
            "{ \"name\" : \"record\", \"type\" : { \"type\" : \"record\", \"name\" : \"Bar\", \"fields\" : [ { \"name\" : \"int\", \"type\" : \"int\" } ] } } \n" +
            "] }");
    private static final RecordDataSchema.Field FIELD_record = SCHEMA.getField("record");

    public Foo()
    {
      super(new DataMap(), SCHEMA);
    }

    public Bar getRecord()
    {
      return obtainWrapped(FIELD_record, Bar.class, GetMode.DEFAULT);
    }

    public void setRecord(Bar value)
    {
      putWrapped(FIELD_record, Bar.class, value);
    }
  }

  public static class Bar extends RecordTemplate
  {
    public static final RecordDataSchema SCHEMA = (RecordDataSchema) DataTemplateUtil.parseSchema
        (
            "{ \"type\" : \"record\", \"name\" : \"Bar\", \"fields\" : [ { \"name\" : \"int\", \"type\" : \"int\" } ] }"
        );
    private static final RecordDataSchema.Field FIELD_int = SCHEMA.getField("int");

    public Bar()
    {
      super(new DataMap(), SCHEMA);
    }

    public Integer getInt()
    {
      return obtainDirect(FIELD_int, Integer.TYPE, GetMode.STRICT);
    }

    public Bar setInt(int value)
    {
      putDirect(FIELD_int, Integer.class, Integer.class, value, SetMode.DISALLOW_NULL);
      return this;
    }
  }
}
