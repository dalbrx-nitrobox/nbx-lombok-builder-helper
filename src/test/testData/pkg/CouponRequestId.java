package pkg;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.*;
import java.util.Objects;

/**
 * CouponRequestId
 */
@JsonTypeName("coupon.RequestId")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", comments = "Generator version: 7.11.0")
public class CouponRequestId {

  @jakarta.validation.constraints.NotBlank
  private String requestId;

  public void build() {
        CouponRequestId.builder().build();
  }

  public CouponRequestId() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CouponRequestId(String requestId) {
    this.requestId = requestId;
  }

  public CouponRequestId requestId(String requestId) {
    this.requestId = requestId;
    return this;
  }

  /**
   * Unique identifier of the creation request.
   * @return requestId
   */
  @NotNull @Size(max = 255) 
  @Schema(name = "requestId", description = "Unique identifier of the creation request.", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("requestId")
  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CouponRequestId couponRequestId = (CouponRequestId) o;
    return Objects.equals(this.requestId, couponRequestId.requestId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requestId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CouponRequestId {\n");
    sb.append("    requestId: ").append(toIndentedString(requestId)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
  
    public static class Builder {
  
      private CouponRequestId instance;
  
      public Builder() {
        this(new CouponRequestId());
      }
  
      protected Builder(CouponRequestId instance) {
        this.instance = instance;
      }
  
      protected Builder copyOf(CouponRequestId value) { 
        this.instance.setRequestId(value.requestId);
        return this;
      }
  
        public CouponRequestId.Builder requestId(String requestId) {
        this.instance.requestId(requestId);
        return this;
      }
      
          /**
      * returns a built CouponRequestId instance.
      *
      * The builder is not reusable (NullPointerException)
      */
      public CouponRequestId build() {
        try {
          return this.instance;
        } finally {
          // ensure that this.instance is not reused
          this.instance = null;
        }
      }
  
      @Override
      public String toString() {
        return getClass() + "=(" + instance + ")";
      }
    }
  
    /**
    * Create a builder with no initialized field (except for the default values).
    */
    public static CouponRequestId.Builder builder() {
      return new CouponRequestId.Builder();
    }
  
    /**
    * Create a builder with a shallow copy of this instance.
    */
    public CouponRequestId.Builder toBuilder() {
      CouponRequestId.Builder builder = new CouponRequestId.Builder();
      return builder.copyOf(this);
    }
}
