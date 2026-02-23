package com.currencyx.config;

import io.grpc.ServerInterceptor;
import io.grpc.Status;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
class GrpcServerConfig {

    private static final Logger logger = LoggerFactory.getLogger(GrpcServerConfig.class);

    @GrpcGlobalServerInterceptor
    ServerInterceptor loggingInterceptor() {
        return new io.grpc.ServerInterceptor() {
            @Override
            public <ReqT, RespT> io.grpc.ServerCall.Listener<ReqT> interceptCall(
                    io.grpc.ServerCall<ReqT, RespT> call,
                    io.grpc.Metadata headers,
                    io.grpc.ServerCallHandler<ReqT, RespT> next) {

                String methodName = call.getMethodDescriptor().getFullMethodName();
                logger.info("gRPC request received: {}", methodName);
                long startTime = System.currentTimeMillis();

                return new io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener<>(
                        next.startCall(
                                new io.grpc.ForwardingServerCall.SimpleForwardingServerCall<>(call) {
                                    @Override
                                    public void close(Status status, io.grpc.Metadata trailers) {
                                        long duration = System.currentTimeMillis() - startTime;
                                        logger.info("gRPC request completed: {} | status={} | duration={}ms",
                                                methodName, status.getCode(), duration);
                                        super.close(status, trailers);
                                    }
                                },
                                headers)) {
                };
            }
        };
    }
}
