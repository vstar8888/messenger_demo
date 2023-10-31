package ru.demo.messenger.network.request;


import java.util.List;

import ru.demo.messenger.data.CompanyModel;
import ru.demo.messenger.network.response.base.BaseLoginResponse;

public class ChooseCompanyResponse extends BaseLoginResponse {
    public List<CompanyModel> companies;
}
