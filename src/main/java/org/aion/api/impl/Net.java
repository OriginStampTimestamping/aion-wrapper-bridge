package org.aion.api.impl;

import com.google.protobuf.InvalidProtocolBufferException;
import org.aion.api.INet;
import org.aion.api.impl.internal.ApiUtils;
import org.aion.api.impl.internal.Message;
import org.aion.api.impl.internal.Message.Funcs;
import org.aion.api.type.ApiMsg;
import org.aion.api.type.ApiMsg.cast;
import org.aion.api.type.Node;
import org.aion.api.type.Protocol;
import org.aion.api.type.SyncInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jay Tseng on 15/11/16.
 */
public class Net implements INet {
    private static final Logger LOGGER = LoggerFactory.getLogger(Net.class.getCanonicalName());
    private AionAPIImpl apiInst;

    Net(AionAPIImpl inst) {
        this.apiInst = inst;
    }

    public ApiMsg syncInfo() {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        byte[] reqHdr =
                ApiUtils.toReqHeader(
                        ApiUtils.PROTOCOL_VER, Message.Servs.s_net, Message.Funcs.f_syncInfo);

        byte[] rsp = this.apiInst.nbProcess(reqHdr);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            Message.rsp_syncInfo msgRsp =
                    Message.rsp_syncInfo.parseFrom(ApiUtils.parseBody(rsp).getData());
            SyncInfo syncInfo =
                    new SyncInfo(
                            msgRsp.getSyncing(),
                            msgRsp.getNetworkBestBlock(),
                            msgRsp.getChainBestBlock(),
                            msgRsp.getMaxImportBlocks(),
                            msgRsp.getStartingBlock());

            return new ApiMsg(syncInfo, ApiMsg.cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(
                        "[syncInfo] {} exception: [{}]", ErrId.getErrString(-104L), e.getMessage());
            }
            return new ApiMsg(-104);
        }
    }

    public ApiMsg isSyncing() {
        if (!apiInst.isInitialized.get()) {
            return new ApiMsg(-1003);
        }

        byte[] reqHdr =
                ApiUtils.toReqHeader(
                        ApiUtils.PROTOCOL_VER, Message.Servs.s_net, Message.Funcs.f_isSyncing);

        byte[] rsp = this.apiInst.nbProcess(reqHdr);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            Message.rsp_isSyncing msgRsp =
                    Message.rsp_isSyncing.parseFrom(ApiUtils.parseBody(rsp).getData());
            return new ApiMsg(msgRsp.getSyncing(), ApiMsg.cast.BOOLEAN);

        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(
                        "[isSyncing] {} exception: [{}]",
                        ErrId.getErrString(-104L),
                        e.getMessage());
            }
            return new ApiMsg(-104, e.getMessage(), ApiMsg.cast.OTHERS);
        }
    }

    public ApiMsg getProtocolVersion() {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        byte[] reqHdr =
                ApiUtils.toReqHeader(
                        ApiUtils.PROTOCOL_VER,
                        Message.Servs.s_net,
                        Message.Funcs.f_protocolVersion);

        byte[] rsp = this.apiInst.nbProcess(reqHdr);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            return new ApiMsg(
                    toProtocol(
                            Message.rsp_protocolVersion.parseFrom(
                                    ApiUtils.parseBody(rsp).getData())),
                    ApiMsg.cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(
                        "[getProtocolVersion] {} exception: [{}]",
                        ErrId.getErrString(-104L),
                        e.getMessage());
            }
            return new ApiMsg(-104);
        }
    }

    private Protocol toProtocol(Message.rsp_protocolVersion rsp_protocolVersion) {
        return new Protocol.ProtocolBuilder()
                .api(rsp_protocolVersion.getApi())
                .db(rsp_protocolVersion.getDb())
                .miner(rsp_protocolVersion.getMiner())
                .kernel(rsp_protocolVersion.getKernel())
                .net(rsp_protocolVersion.getNet())
                .vm(rsp_protocolVersion.getVm())
                .txpool(rsp_protocolVersion.getTxpool())
                .createProtocol();
    }

    public ApiMsg getActiveNodes() {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        byte[] reqHdr =
                ApiUtils.toReqHeader(
                        ApiUtils.PROTOCOL_VER, Message.Servs.s_net, Message.Funcs.f_getActiveNodes);

        byte[] rsp = this.apiInst.nbProcess(reqHdr);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            return new ApiMsg(
                    toNodes(
                            Message.rsp_getActiveNodes
                                    .parseFrom(ApiUtils.parseBody(rsp).getData())
                                    .getNodeList()),
                    ApiMsg.cast.OTHERS);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(
                        "[getActiveNodes] {} exception: [{}]",
                        ErrId.getErrString(-104L),
                        e.getMessage());
            }
            return new ApiMsg(-104);
        }
    }

    private List<Node> toNodes(List<Message.t_Node> nodes) {
        List<Node> l = new ArrayList<>();
        for (Message.t_Node node : nodes) {
            l.add(
                    new Node.NodeBuilder()
                            .blockNumber(node.getBlockNumber())
                            .latency(node.getLatency())
                            .nodeId(node.getNodeId())
                            .p2pIP(node.getRemoteP2PIp())
                            .p2pPort(node.getRemoteP2PPort())
                            .createNode());
        }

        return l;
    }

    public ApiMsg getStaticNodes() {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        byte[] reqHdr =
                ApiUtils.toReqHeader(
                        ApiUtils.PROTOCOL_VER, Message.Servs.s_net, Message.Funcs.f_getStaticNodes);

        byte[] rsp = this.apiInst.nbProcess(reqHdr);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            return new ApiMsg(
                    toNodes(
                            Message.rsp_getStaticNodes
                                    .parseFrom(ApiUtils.parseBody(rsp).getData())
                                    .getNodeList()),
                    ApiMsg.cast.OTHERS);

        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(
                        "[getStaticNodes] {} exception: [{}]",
                        ErrId.getErrString(-104L),
                        e.getMessage());
            }
            return new ApiMsg(-104);
        }
    }

    public ApiMsg isListening() {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        byte[] reqHdr =
                ApiUtils.toReqHeader(ApiUtils.PROTOCOL_VER, Message.Servs.s_net, Funcs.f_listening);

        byte[] rsp = this.apiInst.nbProcess(reqHdr);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            return new ApiMsg(
                    Message.rsp_listening
                            .parseFrom(ApiUtils.parseBody(rsp).getData())
                            .getIsListening(),
                    cast.BOOLEAN);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(
                        "[isListening] {} exception: [{}]",
                        ErrId.getErrString(-104L),
                        e.getMessage());
            }
            return new ApiMsg(-104);
        }
    }

    public ApiMsg getPeerCount() {
        if (!this.apiInst.isConnected()) {
            return new ApiMsg(-1003);
        }

        byte[] reqHdr =
                ApiUtils.toReqHeader(ApiUtils.PROTOCOL_VER, Message.Servs.s_net, Funcs.f_peerCount);

        byte[] rsp = this.apiInst.nbProcess(reqHdr);
        int code = this.apiInst.validRspHeader(rsp);
        if (code != 1) {
            return new ApiMsg(code);
        }

        try {
            return new ApiMsg(
                    1,
                    Message.rsp_peerCount.parseFrom(ApiUtils.parseBody(rsp).getData()).getPeers(),
                    cast.INT);
        } catch (InvalidProtocolBufferException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(
                        "[getPeerCount] {} exception: [{}]",
                        ErrId.getErrString(-104L),
                        e.getMessage());
            }
            return new ApiMsg(-104);
        }
    }
}
