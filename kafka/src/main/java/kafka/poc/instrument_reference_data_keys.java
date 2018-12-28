/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package kafka.poc;

import org.apache.avro.specific.SpecificData;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.SchemaStore;

@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class instrument_reference_data_keys extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = 3142804833889575531L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"instrument_reference_data_keys\",\"namespace\":\"kafka.poc\",\"fields\":[{\"name\":\"inst_id\",\"type\":[\"null\",\"string\"]}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<instrument_reference_data_keys> ENCODER =
      new BinaryMessageEncoder<instrument_reference_data_keys>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<instrument_reference_data_keys> DECODER =
      new BinaryMessageDecoder<instrument_reference_data_keys>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   */
  public static BinaryMessageDecoder<instrument_reference_data_keys> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   */
  public static BinaryMessageDecoder<instrument_reference_data_keys> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<instrument_reference_data_keys>(MODEL$, SCHEMA$, resolver);
  }

  /** Serializes this instrument_reference_data_keys to a ByteBuffer. */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /** Deserializes a instrument_reference_data_keys from a ByteBuffer. */
  public static instrument_reference_data_keys fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

  @Deprecated public java.lang.CharSequence inst_id;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public instrument_reference_data_keys() {}

  /**
   * All-args constructor.
   * @param inst_id The new value for inst_id
   */
  public instrument_reference_data_keys(java.lang.CharSequence inst_id) {
    this.inst_id = inst_id;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return inst_id;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: inst_id = (java.lang.CharSequence)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'inst_id' field.
   * @return The value of the 'inst_id' field.
   */
  public java.lang.CharSequence getInstId() {
    return inst_id;
  }

  /**
   * Sets the value of the 'inst_id' field.
   * @param value the value to set.
   */
  public void setInstId(java.lang.CharSequence value) {
    this.inst_id = value;
  }

  /**
   * Creates a new instrument_reference_data_keys RecordBuilder.
   * @return A new instrument_reference_data_keys RecordBuilder
   */
  public static kafka.poc.instrument_reference_data_keys.Builder newBuilder() {
    return new kafka.poc.instrument_reference_data_keys.Builder();
  }

  /**
   * Creates a new instrument_reference_data_keys RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new instrument_reference_data_keys RecordBuilder
   */
  public static kafka.poc.instrument_reference_data_keys.Builder newBuilder(kafka.poc.instrument_reference_data_keys.Builder other) {
    return new kafka.poc.instrument_reference_data_keys.Builder(other);
  }

  /**
   * Creates a new instrument_reference_data_keys RecordBuilder by copying an existing instrument_reference_data_keys instance.
   * @param other The existing instance to copy.
   * @return A new instrument_reference_data_keys RecordBuilder
   */
  public static kafka.poc.instrument_reference_data_keys.Builder newBuilder(kafka.poc.instrument_reference_data_keys other) {
    return new kafka.poc.instrument_reference_data_keys.Builder(other);
  }

  /**
   * RecordBuilder for instrument_reference_data_keys instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<instrument_reference_data_keys>
    implements org.apache.avro.data.RecordBuilder<instrument_reference_data_keys> {

    private java.lang.CharSequence inst_id;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(kafka.poc.instrument_reference_data_keys.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.inst_id)) {
        this.inst_id = data().deepCopy(fields()[0].schema(), other.inst_id);
        fieldSetFlags()[0] = true;
      }
    }

    /**
     * Creates a Builder by copying an existing instrument_reference_data_keys instance
     * @param other The existing instance to copy.
     */
    private Builder(kafka.poc.instrument_reference_data_keys other) {
            super(SCHEMA$);
      if (isValidValue(fields()[0], other.inst_id)) {
        this.inst_id = data().deepCopy(fields()[0].schema(), other.inst_id);
        fieldSetFlags()[0] = true;
      }
    }

    /**
      * Gets the value of the 'inst_id' field.
      * @return The value.
      */
    public java.lang.CharSequence getInstId() {
      return inst_id;
    }

    /**
      * Sets the value of the 'inst_id' field.
      * @param value The value of 'inst_id'.
      * @return This builder.
      */
    public kafka.poc.instrument_reference_data_keys.Builder setInstId(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.inst_id = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'inst_id' field has been set.
      * @return True if the 'inst_id' field has been set, false otherwise.
      */
    public boolean hasInstId() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'inst_id' field.
      * @return This builder.
      */
    public kafka.poc.instrument_reference_data_keys.Builder clearInstId() {
      inst_id = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public instrument_reference_data_keys build() {
      try {
        instrument_reference_data_keys record = new instrument_reference_data_keys();
        record.inst_id = fieldSetFlags()[0] ? this.inst_id : (java.lang.CharSequence) defaultValue(fields()[0]);
        return record;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<instrument_reference_data_keys>
    WRITER$ = (org.apache.avro.io.DatumWriter<instrument_reference_data_keys>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<instrument_reference_data_keys>
    READER$ = (org.apache.avro.io.DatumReader<instrument_reference_data_keys>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

}
