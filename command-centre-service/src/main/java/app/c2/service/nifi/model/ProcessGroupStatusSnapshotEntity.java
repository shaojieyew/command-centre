package app.c2.service.nifi.model;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

public class ProcessGroupStatusSnapshotEntity  {

    @SerializedName("id")
    private String id = null;
    @SerializedName("canRead")
    private Boolean canRead = false;
    @SerializedName("processGroupStatusSnapshot")
    private ProcessGroupStatusSnapshotDTO processGroupStatusSnapshot = null;

    public ProcessGroupStatusSnapshotEntity() {
    }

    public ProcessGroupStatusSnapshotEntity id(String id) {
        this.id = id;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The id of the process group."
    )
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ApiModelProperty(
            example = "null",
            value = "Indicates whether the user can read a given resource."
    )
    public Boolean getCanRead() {
        return this.canRead;
    }

    public void setCanRead(Boolean canRead) {
        this.canRead = canRead;
    }

    public ProcessGroupStatusSnapshotDTO getProcessGroupStatusSnapshot() {
        return processGroupStatusSnapshot;
    }

    public void setProcessGroupStatusSnapshot(ProcessGroupStatusSnapshotDTO processGroupStatusSnapshot) {
        this.processGroupStatusSnapshot = processGroupStatusSnapshot;
    }

    @Override
    public String toString() {
        return "ProcessGroupStatusSnapshotEntity{" +
                "id='" + id + '\'' +
                ", canRead=" + canRead +
                ", processGroupStatusSnapshot=" + processGroupStatusSnapshot +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProcessGroupStatusSnapshotEntity)) return false;
        ProcessGroupStatusSnapshotEntity that = (ProcessGroupStatusSnapshotEntity) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getCanRead(), that.getCanRead()) &&
                Objects.equals(getProcessGroupStatusSnapshot(), that.getProcessGroupStatusSnapshot());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getCanRead(), getProcessGroupStatusSnapshot());
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
