package app.c2.service.nifi.model;

import com.davis.client.model.ProcessorStatusSnapshotDTO;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class ProcessorStatusSnapshotEntity {

    @SerializedName("id")
    private String id = null;
    @SerializedName("processorStatusSnapshot")
    private ProcessorStatusSnapshotDTO processorStatusSnapshot = null;
    @SerializedName("canRead")
    private Boolean canRead = false;

    public ProcessorStatusSnapshotEntity() {
    }

    public ProcessorStatusSnapshotEntity id(String id) {
        this.id = id;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The id of the processor."
    )
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ProcessorStatusSnapshotEntity processorStatusSnapshot(ProcessorStatusSnapshotDTO processorStatusSnapshot) {
        this.processorStatusSnapshot = processorStatusSnapshot;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = ""
    )
    public ProcessorStatusSnapshotDTO getProcessorStatusSnapshot() {
        return this.processorStatusSnapshot;
    }

    public void setProcessorStatusSnapshot(ProcessorStatusSnapshotDTO processorStatusSnapshot) {
        this.processorStatusSnapshot = processorStatusSnapshot;
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
            ProcessorStatusSnapshotEntity processorStatusSnapshotEntity = (ProcessorStatusSnapshotEntity)o;
            return Objects.equals(this.id, processorStatusSnapshotEntity.id) && Objects.equals(this.processorStatusSnapshot, processorStatusSnapshotEntity.processorStatusSnapshot) && Objects.equals(this.canRead, processorStatusSnapshotEntity.canRead);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.id, this.processorStatusSnapshot, this.canRead});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ProcessorStatusSnapshotEntity {\n");
        sb.append("    id: ").append(this.toIndentedString(this.id)).append("\n");
        sb.append("    processorStatusSnapshot: ").append(this.toIndentedString(this.processorStatusSnapshot)).append("\n");
        sb.append("    canRead: ").append(this.toIndentedString(this.canRead)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}

