//package assignment6;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class Probability {
	
	
	private int budget, machine;
	private Integer[] cost; 
	private Double[] reliability; 
	
	public Probability (String file) {
		
		try {
			FileReader f = new FileReader(file);
			BufferedReader br = new BufferedReader(f);
			
			budget = Integer.parseInt(br.readLine());
			System.out.println("Budget:" + budget);
			
			machine = Integer.parseInt(br.readLine());
			System.out.println("Number Machines:" + machine);
			
			int i = 0, j = 0, c = 0;
			double r = (double) 0.0; 
			String line = null;
			
			cost = new Integer[machine];
			reliability = new Double[machine];
			
			while ((line = br.readLine() ) != null) {	
				String[] parts = line.split("\\s+", 2);
				c = Integer.parseInt(parts[0]);
				cost[i++] = c;
				r = Double.parseDouble(parts[1]);
				reliability[j++] = r;
			}
			f.close();
		} 
		
		catch (Exception e) {
			System.err.print("Error reading file!");
			e.printStackTrace();
		}		
	}
	
	
	public void PrintInput(Integer[] c, Float[] r) {
		
		for(int i=0;i<c.length;i++) {
			System.out.print(c[i]);
			System.out.println(" " + r[i]);
		}
	}
	
	
	public void dpTable() {
		
		System.out.println("Iterated version:");
		
		int totalCost = 0;
		for (int i = 0; i < machine; ++i) {
			totalCost += cost[i];
		}
		int row = machine;
		int column = budget-totalCost+1;		
		double[][] T = new double[row][column];
		int[][] V = new int[row][column];
		
		for (int i = 0; i < row; ++i) {
			for (int j = 0; j < column; ++j) {
				T[i][j] = 0.0;
			}
		}
		T[0][0] = reliability[0];
		for(int i = 1; i < row; i++){
            T[i][0] = T[i-1][0]*reliability[i];
            V[i][0] = 0;
        }
		
		for(int j = 0; j < column; j++){
            int k = (j/cost[0]);
            T[0][j] = (1 - Math.pow((1 - reliability[0]), k + 1));  
            V[0][j] = k;
        }
		
		for(int j = 1; j < column; j++){ 
            for(int i = 1; i < machine; i++) {
                ArrayList<Double> valuesArray = new ArrayList<Double>();
                for(int k = 0; k*cost[i] <= j; k+=1){ 
                    double value = (T[i-1][j-(k*cost[i])])*
                        (1.0-Math.pow((1.0-reliability[i]),k+1));
                    valuesArray.add(value);
                }
                double maxValue = Collections.max(valuesArray);
                int index = valuesArray.indexOf(maxValue);
                T[i][j] = (maxValue >= 0) ? maxValue : 0.0;
                V[i][j] = index;
            }
        }
		System.out.print("Maximum reliability: ");
		System.out.println(T[row-1][column-1]);
		
		int remainingBudget = column - 1;
		for (int i = row -1; i >= 0; --i) {
			System.out.print(V[i][remainingBudget]+1);
			System.out.print(" copies of machine ");
			System.out.print(i+1);
			System.out.print(" of cost ");
			System.out.println(cost[i]);
			remainingBudget -= V[i][remainingBudget]*cost[i];
		}
		
	}
	
	public void dpMemoizationExecute() {
		System.out.println("Memoized version:");
		int totalCost = 0;
		for (int k = 0; k < machine; ++k) {
			totalCost += cost[k];
		}
		Double[][] memoTable = new Double[machine][budget-totalCost+1];
		int[][] valueTable = new int[machine][budget-totalCost+1];
		for (int u=0; u<machine; ++u) {
			for (int v=0; v<budget-totalCost+1; ++v) {
				memoTable[u][v] = -1.0;
				valueTable[u][v] = -1;
			}
		}
		System.out.print("Maximum reliability: ");
		System.out.println(dpMemoization(budget-totalCost, machine-1, memoTable, valueTable));
		
		int remainingBudget = budget - totalCost;
		for (int x = machine -1; x >= 0; --x) {
			System.out.print(valueTable[x][remainingBudget]+1);
			System.out.print(" copies of machine ");
			System.out.print(x+1);
			System.out.print(" of cost ");
			System.out.println(cost[x]);
			remainingBudget -= valueTable[x][remainingBudget]*cost[x];
		}
		
		System.out.println();
		System.out.println("Memoization Statistics:");
		int totalSlots = machine*(budget-totalCost);
		int slotCount = 0;
		
		System.out.print("Total locations: ");
		System.out.println(totalSlots);
		for (int i = 0; i < machine; ++i) {
			for (int j = 0; j <= budget-totalCost; ++j) {
				slotCount += (memoTable[i][j] >= 0.0)? 1: 0;
			}
		}
		System.out.print("Number used: ");
		System.out.println(slotCount);
		System.out.print("Percentage used: ");
		System.out.println((100.0 * slotCount)/totalSlots);
	}
	
	public double dpMemoization(int budget, int index, Double[][] T, int[][] V) {
		if (index == 0 && budget == 0) {
			V[0][0] = 0;
			return reliability[0];
		}
		if (budget == 0 && index > 0) {
			if (T[index][budget] == -1.0) {
				T[index][budget] = dpMemoization(budget, index -1, T, V)*reliability[index];
				V[index][budget] = 0;
			}
			return T[index][budget];
		}
		if (index == 0) {
			V[index][budget] = budget/cost[0];
			return (1 - Math.pow((1 - reliability[index]), (budget/cost[0]) + 1));
		}

		ArrayList<Double> valuesArray = new ArrayList<Double>();
		for(int k = 0; k*cost[index] <= budget; k+=1){
			if (T[index-1][budget-(k*cost[index])] == -1.0) 
				T[index-1][budget-(k*cost[index])] = dpMemoization(budget-(k*cost[index]), index -1, T, V);
			valuesArray.add((1.0-Math.pow((1.0-reliability[index]),k+1))* T[index -1][budget-(k*cost[index])]);
        }
		double maxValue = Collections.max(valuesArray);
        int max_index = valuesArray.indexOf(maxValue);
        V[index][budget] = max_index;
		return maxValue;
	}
	
	public void withoutMemoizationExecute() {
		int totalCost = 0;
		for (int k = 0; k < machine; ++k) {
			totalCost += cost[k];
		}
		System.out.println(withoutMemoization(budget-totalCost, machine-1));
	}
	
	public double withoutMemoization(int budget, int index) {
		if (index == 0 && budget == 0) {
			return reliability[0];
		}
		if (budget == 0 && index > 0) {
			return withoutMemoization(budget, index -1)*reliability[index];
		}
		if (index == 0) {
			return (1 - Math.pow((1 - reliability[index]), (budget/cost[0]) + 1));
		}
		double max_value = 0.0;
		for(int k = 0; k*cost[index] <= budget; k+=1){ 
			max_value = Math.max(max_value,(1.0-Math.pow((1.0-reliability[index]),k+1))*withoutMemoization(budget-(k*cost[index]), index -1));
        }
		return max_value;	
	}
	
	
	public static void main(String[] args) throws IOException {
		Probability r = new Probability(args[0]);
		r.dpTable();
		System.out.println();
		r.dpMemoizationExecute();
		//r.withoutMemoizationExecute();
		
	}

}
