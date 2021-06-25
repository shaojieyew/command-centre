package app.c2.service.nifi.model;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import org.joda.time.DateTime;

import java.util.Objects;

public class ProcessGroupStatusDTO  {
    @SerializedName("id")
    private String id = null;
    @SerializedName("name")
    private String name = null;
    @SerializedName("statsLastRefreshed")
    private DateTime statsLastRefreshed = null;
    @SerializedName("aggregateSnapshot")
    private ProcessGroupStatusSnapshotDTO aggregateSnapshot = null;

    public ProcessGroupStatusDTO() {
    }

    public ProcessGroupStatusDTO id(String id) {
        this.id = id;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The ID of the Process Group"
    )
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ProcessGroupStatusDTO name(String name) {
        this.name = name;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The name of the Process Group"
    )
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProcessGroupStatusDTO statsLastRefreshed(DateTime statsLastRefreshed) {
        this.statsLastRefreshed = statsLastRefreshed;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The time the status for the process group was last refreshed."
    )
    public DateTime getStatsLastRefreshed() {
        return this.statsLastRefreshed;
    }

    public void setStatsLastRefreshed(DateTime statsLastRefreshed) {
        this.statsLastRefreshed = statsLastRefreshed;
    }

    public ProcessGroupStatusDTO aggregateSnapshot(ProcessGroupStatusSnapshotDTO aggregateSnapshot) {
        this.aggregateSnapshot = aggregateSnapshot;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The aggregate status of all nodes in the cluster"
    )
    public ProcessGroupStatusSnapshotDTO getAggregateSnapshot() {
        return this.aggregateSnapshot;
    }

    public void setAggregateSnapshot(ProcessGroupStatusSnapshotDTO aggregateSnapshot) {
        this.aggregateSnapshot = aggregateSnapshot;
    }


    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            ProcessGroupStatusDTO processGroupStatusDTO = (ProcessGroupStatusDTO)o;
            return Objects.equals(this.id, processGroupStatusDTO.id) && Objects.equals(this.name, processGroupStatusDTO.name) && Objects.equals(this.statsLastRefreshed, processGroupStatusDTO.statsLastRefreshed) && Objects.equals(this.aggregateSnapshot, processGroupStatusDTO.aggregateSnapshot);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.id, this.name, this.statsLastRefreshed, this.aggregateSnapshot});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ProcessGroupStatusDTO {\n");
        sb.append("    id: ").append(this.toIndentedString(this.id)).append("\n");
        sb.append("    name: ").append(this.toIndentedString(this.name)).append("\n");
        sb.append("    statsLastRefreshed: ").append(this.toIndentedString(this.statsLastRefreshed)).append("\n");
        sb.append("    aggregateSnapshot: ").append(this.toIndentedString(this.aggregateSnapshot)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }

}
