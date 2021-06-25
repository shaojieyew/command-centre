package app.c2.service.nifi.model;

import com.davis.client.model.ConnectionStatusSnapshotEntity;
import com.davis.client.model.PortStatusSnapshotEntity;
import com.davis.client.model.RemoteProcessGroupStatusSnapshotEntity;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProcessGroupStatusSnapshotDTO {
    @SerializedName("id")
    private String id = null;
    @SerializedName("name")
    private String name = null;
    @SerializedName("connectionStatusSnapshots")
    private List<ConnectionStatusSnapshotEntity> connectionStatusSnapshots = new ArrayList();
    @SerializedName("processorStatusSnapshots")
    private List<ProcessorStatusSnapshotEntity> processorStatusSnapshots = new ArrayList();
    @SerializedName("processGroupStatusSnapshots")
    private List<ProcessGroupStatusSnapshotEntity> processGroupStatusSnapshots = new ArrayList();
    @SerializedName("remoteProcessGroupStatusSnapshots")
    private List<RemoteProcessGroupStatusSnapshotEntity> remoteProcessGroupStatusSnapshots = new ArrayList();
    @SerializedName("inputPortStatusSnapshots")
    private List<PortStatusSnapshotEntity> inputPortStatusSnapshots = new ArrayList();
    @SerializedName("outputPortStatusSnapshots")
    private List<PortStatusSnapshotEntity> outputPortStatusSnapshots = new ArrayList();
    @SerializedName("flowFilesIn")
    private Integer flowFilesIn = null;
    @SerializedName("bytesIn")
    private Long bytesIn = null;
    @SerializedName("input")
    private String input = null;
    @SerializedName("flowFilesQueued")
    private Integer flowFilesQueued = null;
    @SerializedName("bytesQueued")
    private Long bytesQueued = null;
    @SerializedName("queued")
    private String queued = null;
    @SerializedName("queuedCount")
    private String queuedCount = null;
    @SerializedName("queuedSize")
    private String queuedSize = null;
    @SerializedName("bytesRead")
    private Long bytesRead = null;
    @SerializedName("read")
    private String read = null;
    @SerializedName("bytesWritten")
    private Long bytesWritten = null;
    @SerializedName("written")
    private String written = null;
    @SerializedName("flowFilesOut")
    private Integer flowFilesOut = null;
    @SerializedName("bytesOut")
    private Long bytesOut = null;
    @SerializedName("output")
    private String output = null;
    @SerializedName("flowFilesTransferred")
    private Integer flowFilesTransferred = null;
    @SerializedName("bytesTransferred")
    private Long bytesTransferred = null;
    @SerializedName("transferred")
    private String transferred = null;
    @SerializedName("bytesReceived")
    private Long bytesReceived = null;
    @SerializedName("flowFilesReceived")
    private Integer flowFilesReceived = null;
    @SerializedName("received")
    private String received = null;
    @SerializedName("bytesSent")
    private Long bytesSent = null;
    @SerializedName("flowFilesSent")
    private Integer flowFilesSent = null;
    @SerializedName("sent")
    private String sent = null;
    @SerializedName("activeThreadCount")
    private Integer activeThreadCount = null;

    public ProcessGroupStatusSnapshotDTO() {
    }

    public ProcessGroupStatusSnapshotDTO id(String id) {
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

    public ProcessGroupStatusSnapshotDTO name(String name) {
        this.name = name;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The name of this process group."
    )
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProcessGroupStatusSnapshotDTO connectionStatusSnapshots(List<ConnectionStatusSnapshotEntity> connectionStatusSnapshots) {
        this.connectionStatusSnapshots = connectionStatusSnapshots;
        return this;
    }

    public ProcessGroupStatusSnapshotDTO addConnectionStatusSnapshotsItem(ConnectionStatusSnapshotEntity connectionStatusSnapshotsItem) {
        this.connectionStatusSnapshots.add(connectionStatusSnapshotsItem);
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The status of all conenctions in the process group."
    )
    public List<ConnectionStatusSnapshotEntity> getConnectionStatusSnapshots() {
        return this.connectionStatusSnapshots;
    }

    public void setConnectionStatusSnapshots(List<ConnectionStatusSnapshotEntity> connectionStatusSnapshots) {
        this.connectionStatusSnapshots = connectionStatusSnapshots;
    }

    public ProcessGroupStatusSnapshotDTO processorStatusSnapshots(List<ProcessorStatusSnapshotEntity> processorStatusSnapshots) {
        this.processorStatusSnapshots = processorStatusSnapshots;
        return this;
    }

    public ProcessGroupStatusSnapshotDTO addProcessorStatusSnapshotsItem(ProcessorStatusSnapshotEntity processorStatusSnapshotsItem) {
        this.processorStatusSnapshots.add(processorStatusSnapshotsItem);
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The status of all processors in the process group."
    )
    public List<ProcessorStatusSnapshotEntity> getProcessorStatusSnapshots() {
        return this.processorStatusSnapshots;
    }

    public void setProcessorStatusSnapshots(List<ProcessorStatusSnapshotEntity> processorStatusSnapshots) {
        this.processorStatusSnapshots = processorStatusSnapshots;
    }

    public ProcessGroupStatusSnapshotDTO processGroupStatusSnapshots(List<ProcessGroupStatusSnapshotEntity> processGroupStatusSnapshots) {
        this.processGroupStatusSnapshots = processGroupStatusSnapshots;
        return this;
    }

    public ProcessGroupStatusSnapshotDTO addProcessGroupStatusSnapshotsItem(ProcessGroupStatusSnapshotEntity processGroupStatusSnapshotsItem) {
        this.processGroupStatusSnapshots.add(processGroupStatusSnapshotsItem);
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The status of all process groups in the process group."
    )
    public List<ProcessGroupStatusSnapshotEntity> getProcessGroupStatusSnapshots() {
        return this.processGroupStatusSnapshots;
    }

    public void setProcessGroupStatusSnapshots(List<ProcessGroupStatusSnapshotEntity> processGroupStatusSnapshots) {
        this.processGroupStatusSnapshots = processGroupStatusSnapshots;
    }

    public ProcessGroupStatusSnapshotDTO remoteProcessGroupStatusSnapshots(List<RemoteProcessGroupStatusSnapshotEntity> remoteProcessGroupStatusSnapshots) {
        this.remoteProcessGroupStatusSnapshots = remoteProcessGroupStatusSnapshots;
        return this;
    }

    public ProcessGroupStatusSnapshotDTO addRemoteProcessGroupStatusSnapshotsItem(RemoteProcessGroupStatusSnapshotEntity remoteProcessGroupStatusSnapshotsItem) {
        this.remoteProcessGroupStatusSnapshots.add(remoteProcessGroupStatusSnapshotsItem);
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The status of all remote process groups in the process group."
    )
    public List<RemoteProcessGroupStatusSnapshotEntity> getRemoteProcessGroupStatusSnapshots() {
        return this.remoteProcessGroupStatusSnapshots;
    }

    public void setRemoteProcessGroupStatusSnapshots(List<RemoteProcessGroupStatusSnapshotEntity> remoteProcessGroupStatusSnapshots) {
        this.remoteProcessGroupStatusSnapshots = remoteProcessGroupStatusSnapshots;
    }

    public ProcessGroupStatusSnapshotDTO inputPortStatusSnapshots(List<PortStatusSnapshotEntity> inputPortStatusSnapshots) {
        this.inputPortStatusSnapshots = inputPortStatusSnapshots;
        return this;
    }

    public ProcessGroupStatusSnapshotDTO addInputPortStatusSnapshotsItem(PortStatusSnapshotEntity inputPortStatusSnapshotsItem) {
        this.inputPortStatusSnapshots.add(inputPortStatusSnapshotsItem);
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The status of all input ports in the process group."
    )
    public List<PortStatusSnapshotEntity> getInputPortStatusSnapshots() {
        return this.inputPortStatusSnapshots;
    }

    public void setInputPortStatusSnapshots(List<PortStatusSnapshotEntity> inputPortStatusSnapshots) {
        this.inputPortStatusSnapshots = inputPortStatusSnapshots;
    }

    public ProcessGroupStatusSnapshotDTO outputPortStatusSnapshots(List<PortStatusSnapshotEntity> outputPortStatusSnapshots) {
        this.outputPortStatusSnapshots = outputPortStatusSnapshots;
        return this;
    }

    public ProcessGroupStatusSnapshotDTO addOutputPortStatusSnapshotsItem(PortStatusSnapshotEntity outputPortStatusSnapshotsItem) {
        this.outputPortStatusSnapshots.add(outputPortStatusSnapshotsItem);
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The status of all output ports in the process group."
    )
    public List<PortStatusSnapshotEntity> getOutputPortStatusSnapshots() {
        return this.outputPortStatusSnapshots;
    }

    public void setOutputPortStatusSnapshots(List<PortStatusSnapshotEntity> outputPortStatusSnapshots) {
        this.outputPortStatusSnapshots = outputPortStatusSnapshots;
    }

    public ProcessGroupStatusSnapshotDTO flowFilesIn(Integer flowFilesIn) {
        this.flowFilesIn = flowFilesIn;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The number of FlowFiles that have come into this ProcessGroup in the last 5 minutes"
    )
    public Integer getFlowFilesIn() {
        return this.flowFilesIn;
    }

    public void setFlowFilesIn(Integer flowFilesIn) {
        this.flowFilesIn = flowFilesIn;
    }

    public ProcessGroupStatusSnapshotDTO bytesIn(Long bytesIn) {
        this.bytesIn = bytesIn;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The number of bytes that have come into this ProcessGroup in the last 5 minutes"
    )
    public Long getBytesIn() {
        return this.bytesIn;
    }

    public void setBytesIn(Long bytesIn) {
        this.bytesIn = bytesIn;
    }

    public ProcessGroupStatusSnapshotDTO input(String input) {
        this.input = input;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The input count/size for the process group in the last 5 minutes (pretty printed)."
    )
    public String getInput() {
        return this.input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public ProcessGroupStatusSnapshotDTO flowFilesQueued(Integer flowFilesQueued) {
        this.flowFilesQueued = flowFilesQueued;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The number of FlowFiles that are queued up in this ProcessGroup right now"
    )
    public Integer getFlowFilesQueued() {
        return this.flowFilesQueued;
    }

    public void setFlowFilesQueued(Integer flowFilesQueued) {
        this.flowFilesQueued = flowFilesQueued;
    }

    public ProcessGroupStatusSnapshotDTO bytesQueued(Long bytesQueued) {
        this.bytesQueued = bytesQueued;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The number of bytes that are queued up in this ProcessGroup right now"
    )
    public Long getBytesQueued() {
        return this.bytesQueued;
    }

    public void setBytesQueued(Long bytesQueued) {
        this.bytesQueued = bytesQueued;
    }

    public ProcessGroupStatusSnapshotDTO queued(String queued) {
        this.queued = queued;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The count/size that is queued in the the process group."
    )
    public String getQueued() {
        return this.queued;
    }

    public void setQueued(String queued) {
        this.queued = queued;
    }

    public ProcessGroupStatusSnapshotDTO queuedCount(String queuedCount) {
        this.queuedCount = queuedCount;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The count that is queued for the process group."
    )
    public String getQueuedCount() {
        return this.queuedCount;
    }

    public void setQueuedCount(String queuedCount) {
        this.queuedCount = queuedCount;
    }

    public ProcessGroupStatusSnapshotDTO queuedSize(String queuedSize) {
        this.queuedSize = queuedSize;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The size that is queued for the process group."
    )
    public String getQueuedSize() {
        return this.queuedSize;
    }

    public void setQueuedSize(String queuedSize) {
        this.queuedSize = queuedSize;
    }

    public ProcessGroupStatusSnapshotDTO bytesRead(Long bytesRead) {
        this.bytesRead = bytesRead;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The number of bytes read by components in this ProcessGroup in the last 5 minutes"
    )
    public Long getBytesRead() {
        return this.bytesRead;
    }

    public void setBytesRead(Long bytesRead) {
        this.bytesRead = bytesRead;
    }

    public ProcessGroupStatusSnapshotDTO read(String read) {
        this.read = read;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The number of bytes read in the last 5 minutes."
    )
    public String getRead() {
        return this.read;
    }

    public void setRead(String read) {
        this.read = read;
    }

    public ProcessGroupStatusSnapshotDTO bytesWritten(Long bytesWritten) {
        this.bytesWritten = bytesWritten;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The number of bytes written by components in this ProcessGroup in the last 5 minutes"
    )
    public Long getBytesWritten() {
        return this.bytesWritten;
    }

    public void setBytesWritten(Long bytesWritten) {
        this.bytesWritten = bytesWritten;
    }

    public ProcessGroupStatusSnapshotDTO written(String written) {
        this.written = written;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The number of bytes written in the last 5 minutes."
    )
    public String getWritten() {
        return this.written;
    }

    public void setWritten(String written) {
        this.written = written;
    }

    public ProcessGroupStatusSnapshotDTO flowFilesOut(Integer flowFilesOut) {
        this.flowFilesOut = flowFilesOut;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The number of FlowFiles transferred out of this ProcessGroup in the last 5 minutes"
    )
    public Integer getFlowFilesOut() {
        return this.flowFilesOut;
    }

    public void setFlowFilesOut(Integer flowFilesOut) {
        this.flowFilesOut = flowFilesOut;
    }

    public ProcessGroupStatusSnapshotDTO bytesOut(Long bytesOut) {
        this.bytesOut = bytesOut;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The number of bytes transferred out of this ProcessGroup in the last 5 minutes"
    )
    public Long getBytesOut() {
        return this.bytesOut;
    }

    public void setBytesOut(Long bytesOut) {
        this.bytesOut = bytesOut;
    }

    public ProcessGroupStatusSnapshotDTO output(String output) {
        this.output = output;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The output count/size for the process group in the last 5 minutes."
    )
    public String getOutput() {
        return this.output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public ProcessGroupStatusSnapshotDTO flowFilesTransferred(Integer flowFilesTransferred) {
        this.flowFilesTransferred = flowFilesTransferred;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The number of FlowFiles transferred in this ProcessGroup in the last 5 minutes"
    )
    public Integer getFlowFilesTransferred() {
        return this.flowFilesTransferred;
    }

    public void setFlowFilesTransferred(Integer flowFilesTransferred) {
        this.flowFilesTransferred = flowFilesTransferred;
    }

    public ProcessGroupStatusSnapshotDTO bytesTransferred(Long bytesTransferred) {
        this.bytesTransferred = bytesTransferred;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The number of bytes transferred in this ProcessGroup in the last 5 minutes"
    )
    public Long getBytesTransferred() {
        return this.bytesTransferred;
    }

    public void setBytesTransferred(Long bytesTransferred) {
        this.bytesTransferred = bytesTransferred;
    }

    public ProcessGroupStatusSnapshotDTO transferred(String transferred) {
        this.transferred = transferred;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The count/size transferred to/frome queues in the process group in the last 5 minutes."
    )
    public String getTransferred() {
        return this.transferred;
    }

    public void setTransferred(String transferred) {
        this.transferred = transferred;
    }

    public ProcessGroupStatusSnapshotDTO bytesReceived(Long bytesReceived) {
        this.bytesReceived = bytesReceived;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The number of bytes received from external sources by components within this ProcessGroup in the last 5 minutes"
    )
    public Long getBytesReceived() {
        return this.bytesReceived;
    }

    public void setBytesReceived(Long bytesReceived) {
        this.bytesReceived = bytesReceived;
    }

    public ProcessGroupStatusSnapshotDTO flowFilesReceived(Integer flowFilesReceived) {
        this.flowFilesReceived = flowFilesReceived;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The number of FlowFiles received from external sources by components within this ProcessGroup in the last 5 minutes"
    )
    public Integer getFlowFilesReceived() {
        return this.flowFilesReceived;
    }

    public void setFlowFilesReceived(Integer flowFilesReceived) {
        this.flowFilesReceived = flowFilesReceived;
    }

    public ProcessGroupStatusSnapshotDTO received(String received) {
        this.received = received;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The count/size sent to the process group in the last 5 minutes."
    )
    public String getReceived() {
        return this.received;
    }

    public void setReceived(String received) {
        this.received = received;
    }

    public ProcessGroupStatusSnapshotDTO bytesSent(Long bytesSent) {
        this.bytesSent = bytesSent;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The number of bytes sent to an external sink by components within this ProcessGroup in the last 5 minutes"
    )
    public Long getBytesSent() {
        return this.bytesSent;
    }

    public void setBytesSent(Long bytesSent) {
        this.bytesSent = bytesSent;
    }

    public ProcessGroupStatusSnapshotDTO flowFilesSent(Integer flowFilesSent) {
        this.flowFilesSent = flowFilesSent;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The number of FlowFiles sent to an external sink by components within this ProcessGroup in the last 5 minutes"
    )
    public Integer getFlowFilesSent() {
        return this.flowFilesSent;
    }

    public void setFlowFilesSent(Integer flowFilesSent) {
        this.flowFilesSent = flowFilesSent;
    }

    public ProcessGroupStatusSnapshotDTO sent(String sent) {
        this.sent = sent;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The count/size sent from this process group in the last 5 minutes."
    )
    public String getSent() {
        return this.sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }

    public ProcessGroupStatusSnapshotDTO activeThreadCount(Integer activeThreadCount) {
        this.activeThreadCount = activeThreadCount;
        return this;
    }

    @ApiModelProperty(
            example = "null",
            value = "The active thread count for this process group."
    )
    public Integer getActiveThreadCount() {
        return this.activeThreadCount;
    }

    public void setActiveThreadCount(Integer activeThreadCount) {
        this.activeThreadCount = activeThreadCount;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            ProcessGroupStatusSnapshotDTO processGroupStatusSnapshotDTO = (ProcessGroupStatusSnapshotDTO)o;
            return Objects.equals(this.id, processGroupStatusSnapshotDTO.id) && Objects.equals(this.name, processGroupStatusSnapshotDTO.name) && Objects.equals(this.connectionStatusSnapshots, processGroupStatusSnapshotDTO.connectionStatusSnapshots) && Objects.equals(this.processorStatusSnapshots, processGroupStatusSnapshotDTO.processorStatusSnapshots) && Objects.equals(this.processGroupStatusSnapshots, processGroupStatusSnapshotDTO.processGroupStatusSnapshots) && Objects.equals(this.remoteProcessGroupStatusSnapshots, processGroupStatusSnapshotDTO.remoteProcessGroupStatusSnapshots) && Objects.equals(this.inputPortStatusSnapshots, processGroupStatusSnapshotDTO.inputPortStatusSnapshots) && Objects.equals(this.outputPortStatusSnapshots, processGroupStatusSnapshotDTO.outputPortStatusSnapshots) && Objects.equals(this.flowFilesIn, processGroupStatusSnapshotDTO.flowFilesIn) && Objects.equals(this.bytesIn, processGroupStatusSnapshotDTO.bytesIn) && Objects.equals(this.input, processGroupStatusSnapshotDTO.input) && Objects.equals(this.flowFilesQueued, processGroupStatusSnapshotDTO.flowFilesQueued) && Objects.equals(this.bytesQueued, processGroupStatusSnapshotDTO.bytesQueued) && Objects.equals(this.queued, processGroupStatusSnapshotDTO.queued) && Objects.equals(this.queuedCount, processGroupStatusSnapshotDTO.queuedCount) && Objects.equals(this.queuedSize, processGroupStatusSnapshotDTO.queuedSize) && Objects.equals(this.bytesRead, processGroupStatusSnapshotDTO.bytesRead) && Objects.equals(this.read, processGroupStatusSnapshotDTO.read) && Objects.equals(this.bytesWritten, processGroupStatusSnapshotDTO.bytesWritten) && Objects.equals(this.written, processGroupStatusSnapshotDTO.written) && Objects.equals(this.flowFilesOut, processGroupStatusSnapshotDTO.flowFilesOut) && Objects.equals(this.bytesOut, processGroupStatusSnapshotDTO.bytesOut) && Objects.equals(this.output, processGroupStatusSnapshotDTO.output) && Objects.equals(this.flowFilesTransferred, processGroupStatusSnapshotDTO.flowFilesTransferred) && Objects.equals(this.bytesTransferred, processGroupStatusSnapshotDTO.bytesTransferred) && Objects.equals(this.transferred, processGroupStatusSnapshotDTO.transferred) && Objects.equals(this.bytesReceived, processGroupStatusSnapshotDTO.bytesReceived) && Objects.equals(this.flowFilesReceived, processGroupStatusSnapshotDTO.flowFilesReceived) && Objects.equals(this.received, processGroupStatusSnapshotDTO.received) && Objects.equals(this.bytesSent, processGroupStatusSnapshotDTO.bytesSent) && Objects.equals(this.flowFilesSent, processGroupStatusSnapshotDTO.flowFilesSent) && Objects.equals(this.sent, processGroupStatusSnapshotDTO.sent) && Objects.equals(this.activeThreadCount, processGroupStatusSnapshotDTO.activeThreadCount);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.id, this.name, this.connectionStatusSnapshots, this.processorStatusSnapshots, this.processGroupStatusSnapshots, this.remoteProcessGroupStatusSnapshots, this.inputPortStatusSnapshots, this.outputPortStatusSnapshots, this.flowFilesIn, this.bytesIn, this.input, this.flowFilesQueued, this.bytesQueued, this.queued, this.queuedCount, this.queuedSize, this.bytesRead, this.read, this.bytesWritten, this.written, this.flowFilesOut, this.bytesOut, this.output, this.flowFilesTransferred, this.bytesTransferred, this.transferred, this.bytesReceived, this.flowFilesReceived, this.received, this.bytesSent, this.flowFilesSent, this.sent, this.activeThreadCount});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ProcessGroupStatusSnapshotDTO {\n");
        sb.append("    id: ").append(this.toIndentedString(this.id)).append("\n");
        sb.append("    name: ").append(this.toIndentedString(this.name)).append("\n");
        sb.append("    connectionStatusSnapshots: ").append(this.toIndentedString(this.connectionStatusSnapshots)).append("\n");
        sb.append("    processorStatusSnapshots: ").append(this.toIndentedString(this.processorStatusSnapshots)).append("\n");
        sb.append("    processGroupStatusSnapshots: ").append(this.toIndentedString(this.processGroupStatusSnapshots)).append("\n");
        sb.append("    remoteProcessGroupStatusSnapshots: ").append(this.toIndentedString(this.remoteProcessGroupStatusSnapshots)).append("\n");
        sb.append("    inputPortStatusSnapshots: ").append(this.toIndentedString(this.inputPortStatusSnapshots)).append("\n");
        sb.append("    outputPortStatusSnapshots: ").append(this.toIndentedString(this.outputPortStatusSnapshots)).append("\n");
        sb.append("    flowFilesIn: ").append(this.toIndentedString(this.flowFilesIn)).append("\n");
        sb.append("    bytesIn: ").append(this.toIndentedString(this.bytesIn)).append("\n");
        sb.append("    input: ").append(this.toIndentedString(this.input)).append("\n");
        sb.append("    flowFilesQueued: ").append(this.toIndentedString(this.flowFilesQueued)).append("\n");
        sb.append("    bytesQueued: ").append(this.toIndentedString(this.bytesQueued)).append("\n");
        sb.append("    queued: ").append(this.toIndentedString(this.queued)).append("\n");
        sb.append("    queuedCount: ").append(this.toIndentedString(this.queuedCount)).append("\n");
        sb.append("    queuedSize: ").append(this.toIndentedString(this.queuedSize)).append("\n");
        sb.append("    bytesRead: ").append(this.toIndentedString(this.bytesRead)).append("\n");
        sb.append("    read: ").append(this.toIndentedString(this.read)).append("\n");
        sb.append("    bytesWritten: ").append(this.toIndentedString(this.bytesWritten)).append("\n");
        sb.append("    written: ").append(this.toIndentedString(this.written)).append("\n");
        sb.append("    flowFilesOut: ").append(this.toIndentedString(this.flowFilesOut)).append("\n");
        sb.append("    bytesOut: ").append(this.toIndentedString(this.bytesOut)).append("\n");
        sb.append("    output: ").append(this.toIndentedString(this.output)).append("\n");
        sb.append("    flowFilesTransferred: ").append(this.toIndentedString(this.flowFilesTransferred)).append("\n");
        sb.append("    bytesTransferred: ").append(this.toIndentedString(this.bytesTransferred)).append("\n");
        sb.append("    transferred: ").append(this.toIndentedString(this.transferred)).append("\n");
        sb.append("    bytesReceived: ").append(this.toIndentedString(this.bytesReceived)).append("\n");
        sb.append("    flowFilesReceived: ").append(this.toIndentedString(this.flowFilesReceived)).append("\n");
        sb.append("    received: ").append(this.toIndentedString(this.received)).append("\n");
        sb.append("    bytesSent: ").append(this.toIndentedString(this.bytesSent)).append("\n");
        sb.append("    flowFilesSent: ").append(this.toIndentedString(this.flowFilesSent)).append("\n");
        sb.append("    sent: ").append(this.toIndentedString(this.sent)).append("\n");
        sb.append("    activeThreadCount: ").append(this.toIndentedString(this.activeThreadCount)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
