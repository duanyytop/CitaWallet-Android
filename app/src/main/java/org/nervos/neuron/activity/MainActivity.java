package org.nervos.neuron.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.activity.transfer.TransferActivity;
import org.nervos.neuron.fragment.AppFragment;
import org.nervos.neuron.fragment.SettingsFragment;
import org.nervos.neuron.fragment.wallet.WalletFragment;
import org.nervos.neuron.item.Token;
import org.nervos.neuron.service.http.AppChainRpcService;
import org.nervos.neuron.service.http.EthRpcService;
import org.nervos.neuron.service.intent.AppChainTransactionCheckService;
import org.nervos.neuron.service.intent.EtherTransactionCheckService;
import org.nervos.neuron.util.ConstantUtil;
import org.nervos.neuron.util.db.DBWalletUtil;
import org.nervos.neuron.util.qrcode.CodeUtils;
import org.nervos.neuron.util.url.HttpAppChainUrls;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by duanyytop on 2018/4/17
 */
public class MainActivity extends NBaseActivity {

    public static final String EXTRA_TAG = "extra_tag";
    public static final int REQUEST_CODE_SCAN = 0x01;
    private static final int APPCAHIN_TRANSACTION_FETCH_PERIOD = 300000;
    private static final int ETHER_TRANSACTION_FETCH_PERIOD = 300000;

    private RadioGroup navigation;
    private AppFragment appFragment;
    private WalletFragment walletFragment;
    private SettingsFragment settingsFragment;
    private FragmentManager fMgr;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected int getStatusBarColor() {
        return getResources().getColor(R.color.white);
    }

    @Override
    protected void initView() {
        navigation = findViewById(R.id.navigation);
        navigation.check(RadioGroup.NO_ID);

    }

    @Override
    protected void initData() {
        fMgr = getSupportFragmentManager();

        startCheckAppChainTransaction();
        startCheckEtherTransaction();
    }

    private void startCheckAppChainTransaction() {
        AppChainRpcService.init(mActivity, HttpAppChainUrls.APPCHAIN_NODE_URL);
        Intent intent = new Intent();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                AppChainTransactionCheckService.enqueueWork(mActivity, intent);
            }
        }, 0, APPCAHIN_TRANSACTION_FETCH_PERIOD);
    }

    private void startCheckEtherTransaction() {
        EthRpcService.initNodeUrl();
        Intent intent = new Intent();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                EtherTransactionCheckService.enqueueWork(mActivity, intent);
            }
        }, 0, ETHER_TRANSACTION_FETCH_PERIOD);
    }

    @Override
    protected void initAction() {
        navigation.setOnCheckedChangeListener((group, checkedId) -> {
            FragmentTransaction transaction = fMgr.beginTransaction();
            hideFragments(transaction);
            switch (checkedId) {
                case R.id.navigation_application:
                    if (appFragment == null) {
                        appFragment = new AppFragment();
                        transaction.add(R.id.fragment, appFragment);
                    } else {
                        transaction.show(appFragment);
                    }
                    break;
                case R.id.navigation_wallet:
                    if (DBWalletUtil.getCurrentWallet(mActivity) == null) {
                        startActivity(new Intent(mActivity, AddWalletActivity.class));
                    } else {
                        if (walletFragment == null) {
                            walletFragment = new WalletFragment();
                            transaction.add(R.id.fragment, walletFragment);
                        } else {
                            transaction.show(walletFragment);
                        }
                    }
                    break;
                case R.id.navigation_settings:
                    if (settingsFragment == null) {
                        settingsFragment = new SettingsFragment();
                        transaction.add(R.id.fragment, settingsFragment);
                    } else {
                        transaction.show(settingsFragment);
                    }
                    break;
                default:
                    if (appFragment == null) {
                        appFragment = new AppFragment();
                        transaction.add(R.id.fragment, appFragment);
                    } else {
                        transaction.show(appFragment);
                    }
                    break;
            }
            transaction.commitAllowingStateLoss();
        });

        setNavigationItem(AppFragment.Companion.getTAG());

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent() != null) {
            setNavigationItem(getIntent().getStringExtra(EXTRA_TAG));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setNavigationItem(intent.getStringExtra(EXTRA_TAG));
    }

    /**
     * Go to fragment with flag
     *
     * @param tag Fragment flag
     */
    public void setNavigationItem(String tag) {
        if (TextUtils.isEmpty(tag)) return;
        if (TextUtils.equals(tag, AppFragment.Companion.getTAG())) {
            navigation.check(R.id.navigation_application);
        } else if (TextUtils.equals(tag, WalletFragment.Companion.getTAG())) {
            navigation.check(R.id.navigation_wallet);
        } else if (TextUtils.equals(tag, SettingsFragment.TAG)) {
            navigation.check(R.id.navigation_settings);
        }
    }


    /**
     * 隐藏所有的fragment
     */
    private void hideFragments(FragmentTransaction transaction) {
        if (appFragment != null) {
            transaction.hide(appFragment);
        }
        if (walletFragment != null) {
            transaction.hide(walletFragment);
        }
        if (settingsFragment != null) {
            transaction.hide(settingsFragment);
        }
    }


    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (appFragment.isVisible()) {
                if (appFragment.canGoBack()) {
                    appFragment.goBack();
                    return true;
                } else {
                    if ((System.currentTimeMillis() - exitTime) > 2000) {
                        Toast.makeText(getApplicationContext(), R.string.press_back_finish,
                                Toast.LENGTH_SHORT).show();
                        exitTime = System.currentTimeMillis();
                        return false;
                    } else {
                        finish();
                        return true;
                    }
                }
            } else {
                FragmentTransaction fragmentTransaction = fMgr.beginTransaction();
                hideFragments(fragmentTransaction);
                setNavigationItem(AppFragment.Companion.getTAG());
                return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
            switch (requestCode) {
                case REQUEST_CODE_SCAN:
                    if (null != data) {
                        Bundle bundle = data.getExtras();
                        if (bundle == null) return;
                        if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                            String result = bundle.getString(CodeUtils.RESULT_STRING);
                            Intent intent;
                            switch (bundle.getInt(CodeUtils.STRING_TYPE)) {
                                case CodeUtils.STRING_UNVALID:
                                    Toast.makeText(this, R.string.address_error, Toast.LENGTH_LONG).show();
                                    break;
                                case CodeUtils.STRING_ADDRESS:
                                    Token token = new Token(ConstantUtil.ETH_MAINNET, ConstantUtil.ETH, ConstantUtil.ETHEREUM_MAIN_ID);
                                    intent = new Intent(mActivity, TransferActivity.class);
                                    intent.putExtra(TransferActivity.EXTRA_TOKEN, token);
                                    intent.putExtra(TransferActivity.EXTRA_ADDRESS, result);
                                    startActivity(intent);
                                    break;
                                case CodeUtils.STRING_KEYSTORE:
                                    intent = new Intent(this, ImportWalletActivity.class);
                                    intent.putExtra(ImportWalletActivity.INTENT_FROM, ImportWalletActivity.INTENT_FROM_VALUE);
                                    intent.putExtra(ImportWalletActivity.QR_CODE_TYPE, ImportWalletActivity.QR_CODE_TYPE_KEYSTORE);
                                    intent.putExtra(ImportWalletActivity.QR_CODE_VALUE, result);
                                    startActivity(intent);
                                    break;
                                case CodeUtils.STRING_WEB:
                                    SimpleWebActivity.gotoSimpleWeb(this, result);
                                    break;
                                case CodeUtils.STRING_PRIVATE_KEY:
                                    intent = new Intent(this, ImportWalletActivity.class);
                                    intent.putExtra(ImportWalletActivity.INTENT_FROM, ImportWalletActivity.INTENT_FROM_VALUE);
                                    intent.putExtra(ImportWalletActivity.QR_CODE_TYPE, ImportWalletActivity.QR_CODE_TYPE_PRIVATEKEY);
                                    intent.putExtra(ImportWalletActivity.QR_CODE_VALUE, result);
                                    startActivity(intent);
                                    break;
                            }
                        } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                            Toast.makeText(this, R.string.qrcode_handle_fail, Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
                default:
                    break;
            }
    }

    @Override
    public void finish() {
        System.exit(0);
        super.finish();
    }
}
