package app.c2.service.nifi.model;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class ProcessGroupStatusEntityV2 {

    @SerializedName("processGroupStatus")
    private ProcessGroupStatusDTO processGroupStatus = null;
    @SerializedName("canRead")
    private Boolean canRead = false;

    public ProcessGroupStatusEntityV2() {
    }

    public ProcessGroupStatusEntityV2 processGroupStatus(ProcessGroupStatusDTO processGroupStatus) {
        this.processGroupStatus = processGroupStatus;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = ""
    )
    public ProcessGroupStatusDTO getProcessGroupStatus() {
        return this.processGroupStatus;
    }

    public void setProcessGroupStatus(ProcessGroupStatusDTO processGroupStatus) {
        this.processGroupStatus = processGroupStatus;
    }

    @ApiModelProperty(
            example = "null",
            value = "Indicates whether the user can read a given resource."
    )
    public Boolean getCanRead() {
        return this.canRead;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            ProcessGroupStatusEntityV2 processGroupStatusEntityV2 = (ProcessGroupStatusEntityV2)o;
            return Objects.equals(this.processGroupStatus, processGroupStatusEntityV2.processGroupStatus) && Objects.equals(this.canRead, processGroupStatusEntityV2.canRead);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.processGroupStatus, this.canRead});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ProcessGroupStatusEntity {\n");
        sb.append("    processGroupStatus: ").append(this.toIndentedString(this.processGroupStatus)).append("\n");
        sb.append("    canRead: ").append(this.toIndentedString(this.canRead)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
