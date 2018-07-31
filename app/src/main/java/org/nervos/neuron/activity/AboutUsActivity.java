package org.nervos.neuron.activity;

import android.content.pm.PackageManager;
import android.widget.TextView;
import android.widget.Toast;

import org.nervos.neuron.R;
import org.nervos.neuron.custom.SettingButtonView;
import org.nervos.neuron.custom.TitleBar;
import org.nervos.neuron.util.ConstUtil;

/**
 * Created by 包俊 on 2018/7/30.
 */
public class AboutUsActivity extends NBaseActivity {

    private TextView versionText;
    private TitleBar title;
    private SettingButtonView sourceCodeSBV, serverPrivateSBV, NervosSBV, InfuaSBV, openSeaSBV;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_about_us;
    }

    @Override
    protected void initView() {
        versionText = findViewById(R.id.app_version);
        sourceCodeSBV = findViewById(R.id.sbv_source_code);
        serverPrivateSBV = findViewById(R.id.sbv_server_private);
        NervosSBV = findViewById(R.id.sbv_nervos_network);
        InfuaSBV = findViewById(R.id.sbv_infua);
        openSeaSBV = findViewById(R.id.sbv_open_sea);
        title = findViewById(R.id.title);
    }

    @Override
    protected void initAction() {
        initListener();
    }

    @Override
    protected void initData() {
        try {
            String versionName = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
            versionText.setText(String.format("V %s", versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        title.setLeftImage(R.drawable.ic_toptitle_back_white);
    }

    private void initListener() {
        sourceCodeSBV.setOpenListener(() -> {
            SimpleWebActivity.gotoSimpleWeb(AboutUsActivity.this, ConstUtil.SOURCE_CODE_GITHUB_URL);
        });
        serverPrivateSBV.setOpenListener(() -> {
            SimpleWebActivity.gotoSimpleWeb(AboutUsActivity.this, ConstUtil.PRODUCT_AGREEMENT_URL);
        });

    }
}