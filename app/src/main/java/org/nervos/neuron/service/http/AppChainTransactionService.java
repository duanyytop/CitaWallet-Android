package org.nervos.neuron.service.http;

import android.content.Context;
import android.text.TextUtils;

import org.nervos.appchain.protocol.core.methods.response.TransactionReceipt;
import org.nervos.appchain.utils.Numeric;
import org.nervos.neuron.item.AppChainTransactionDBItem;
import org.nervos.neuron.item.TransactionItem;
import org.nervos.neuron.util.db.DBAppChainTransactionsUtil;
import org.nervos.neuron.util.db.DBAppChainTransactionsUtil.TokenType;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by BaojunCZ on 2018/10/11.
 */
public class AppChainTransactionService {
    private static Observable<AppChainTransactionDBItem> query(Context context, boolean pending, TokenType type, String contractAddress) {
        List<AppChainTransactionDBItem> list = DBAppChainTransactionsUtil.getAllTransactions(context, pending, type, contractAddress);
        return Observable.from(list)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread());
    }

    public static void checkResult(Context context, CheckImpl impl) {
        query(context, true, TokenType.ALL, "")
                .filter(item -> {
                    if (TextUtils.isEmpty(item.chain)) {
                        DBAppChainTransactionsUtil.deletePending(context, item);
                    } else {
                        AppChainRpcService.setHttpProvider(item.chain);
                    }
                    return !TextUtils.isEmpty(item.chain);
                })
                .subscribe(new NeuronSubscriber<AppChainTransactionDBItem>() {
                    @Override
                    public void onError(Throwable e) {
                        impl.checkFinish();
                    }
                    @Override
                    public void onNext(AppChainTransactionDBItem item) {
                        impl.checkFinish();
                        TransactionReceipt receipt = AppChainRpcService.getTransactionReceipt(item.hash);
                        if (receipt != null) {
                            if (!TextUtils.isEmpty(receipt.getErrorMessage())) {
                                DBAppChainTransactionsUtil.deletePending(context, item);
                            } else if (Numeric.decodeQuantity(item.validUntilBlock).compareTo(AppChainRpcService.getBlockNumber()) < 0) {
                                DBAppChainTransactionsUtil.failed(context, item);
                            }
                        }
                    }
                });
    }

    public static List<TransactionItem> getTransactionList(Context context, TokenType type, String chain,
                                                           String contractAddress, List<TransactionItem> list) {
        List<AppChainTransactionDBItem> allList = DBAppChainTransactionsUtil.getAllTransactionWithChain(context, chain, type, contractAddress);
        if (allList.size() > 0) {
            for (AppChainTransactionDBItem item : allList) {
                for (TransactionItem transactionItem : list) {
                    if (transactionItem.hash.equalsIgnoreCase(item.hash) && transactionItem.from.equalsIgnoreCase(item.from)) {
                        list.add(new TransactionItem(item.from, item.to, item.value, item.chainName, item.status, item.timestamp, item.hash));
                        break;
                    }
                }
            }
            Collections.sort(list, (o1, o2) -> o2.getDate().compareTo(o1.getDate()));
        }
        return list;
    }

    public interface CheckImpl {
        void checkFinish();
    }

}
