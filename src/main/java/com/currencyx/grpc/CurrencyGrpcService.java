package com.currencyx.grpc;

import com.currencyx.config.AppConstants;
import com.currencyx.exception.CurrencyNotFoundException;
import com.currencyx.exception.InvalidAmountException;
import com.currencyx.exception.RateUnavailableException;
import com.currencyx.grpc.proto.ConvertRequest;
import com.currencyx.grpc.proto.ConvertResponse;
import com.currencyx.grpc.proto.CurrencyServiceGrpc;
import com.currencyx.grpc.proto.HistoricalRate;
import com.currencyx.grpc.proto.RateHistoryRequest;
import com.currencyx.grpc.proto.RateHistoryResponse;
import com.currencyx.grpc.proto.RateRequest;
import com.currencyx.grpc.proto.RateResponse;
import com.currencyx.model.ConversionResult;
import com.currencyx.model.CurrencyRate;
import com.currencyx.model.RateHistory;
import com.currencyx.service.ExchangeRateService;
import com.currencyx.service.RateHistoryService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

@GrpcService
class CurrencyGrpcService extends CurrencyServiceGrpc.CurrencyServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyGrpcService.class);

    private final ExchangeRateService exchangeRateService;
    private final RateHistoryService rateHistoryService;

    CurrencyGrpcService(ExchangeRateService exchangeRateService,
                        RateHistoryService rateHistoryService) {
        this.exchangeRateService = exchangeRateService;
        this.rateHistoryService = rateHistoryService;
    }

    @Override
    public void getRate(RateRequest request, StreamObserver<RateResponse> responseObserver) {
        logger.info("gRPC GetRate: {}/{}", request.getFromCurrency(), request.getToCurrency());
        try {
            CurrencyRate rate = exchangeRateService.fetchLatestExchangeRateForPair(
                    request.getFromCurrency(), request.getToCurrency());

            RateResponse response = RateResponse.newBuilder()
                    .setFromCurrency(rate.getFromCurrency())
                    .setToCurrency(rate.getToCurrency())
                    .setRate(rate.getRate().toPlainString())
                    .setTimestamp(rate.getTimestamp().toEpochMilli())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (CurrencyNotFoundException ex) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(ex.getMessage())
                    .asRuntimeException());
        } catch (RateUnavailableException ex) {
            responseObserver.onError(Status.UNAVAILABLE
                    .withDescription(ex.getMessage())
                    .asRuntimeException());
        } catch (Exception ex) {
            logger.error("Unexpected error in gRPC GetRate", ex);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .asRuntimeException());
        }
    }

    @Override
    public void convert(ConvertRequest request, StreamObserver<ConvertResponse> responseObserver) {
        logger.info("gRPC Convert: {} {} -> {}",
                request.getAmount(), request.getFromCurrency(), request.getToCurrency());
        try {
            BigDecimal amount = new BigDecimal(request.getAmount());
            ConversionResult result = exchangeRateService.performCurrencyConversion(
                    request.getFromCurrency(), request.getToCurrency(), amount);

            ConvertResponse response = ConvertResponse.newBuilder()
                    .setFromCurrency(result.getFromCurrency())
                    .setToCurrency(result.getToCurrency())
                    .setOriginalAmount(result.getOriginalAmount().toPlainString())
                    .setConvertedAmount(result.getConvertedAmount().toPlainString())
                    .setRate(result.getAppliedRate().toPlainString())
                    .setTimestamp(result.getTimestamp().toEpochMilli())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NumberFormatException ex) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("Invalid amount format: " + request.getAmount())
                    .asRuntimeException());
        } catch (InvalidAmountException ex) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(ex.getMessage())
                    .asRuntimeException());
        } catch (CurrencyNotFoundException ex) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(ex.getMessage())
                    .asRuntimeException());
        } catch (Exception ex) {
            logger.error("Unexpected error in gRPC Convert", ex);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .asRuntimeException());
        }
    }

    @Override
    public void getRateHistory(RateHistoryRequest request,
                               StreamObserver<RateHistoryResponse> responseObserver) {
        logger.info("gRPC GetRateHistory: {}/{} (limit={})",
                request.getFromCurrency(), request.getToCurrency(), request.getLimit());
        try {
            int limit = request.getLimit() > 0 ? request.getLimit() : AppConstants.DEFAULT_HISTORY_LIMIT;
            RateHistory history = rateHistoryService.retrieveRateHistoryForCurrencyPair(
                    request.getFromCurrency(), request.getToCurrency(), limit);

            RateHistoryResponse.Builder responseBuilder = RateHistoryResponse.newBuilder()
                    .setFromCurrency(history.getFromCurrency())
                    .setToCurrency(history.getToCurrency());

            for (CurrencyRate rate : history.retrieveUnmodifiableRateList()) {
                HistoricalRate historicalEntry = HistoricalRate.newBuilder()
                        .setRate(rate.getRate().toPlainString())
                        .setTimestamp(rate.getTimestamp().toEpochMilli())
                        .build();
                responseBuilder.addRates(historicalEntry);
            }

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
        } catch (CurrencyNotFoundException ex) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(ex.getMessage())
                    .asRuntimeException());
        } catch (Exception ex) {
            logger.error("Unexpected error in gRPC GetRateHistory", ex);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .asRuntimeException());
        }
    }
}
