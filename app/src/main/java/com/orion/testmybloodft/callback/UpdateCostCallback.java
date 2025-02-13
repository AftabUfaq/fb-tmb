package com.orion.testmybloodft.callback;

public interface UpdateCostCallback {

	void update_prod_cost(int prodCost);
	void update_total_cost(int totalCost);
	void after_delete_prod_total_cost(int cost);

}
