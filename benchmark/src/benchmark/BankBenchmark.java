package benchmark;

import java.util.concurrent.locks.ReentrantLock;

import benchmark.TestThread;

public class BankBenchmark {

	protected static final int INITIAL_BALANCE = 1000;
	protected int numAccounts = BankApp.DEFAULT_NUM_ACCOUNTS;
	protected static ReentrantLock lock = new ReentrantLock();

	int l0 = INITIAL_BALANCE;
	int l1 = INITIAL_BALANCE;
	int l2 = INITIAL_BALANCE;
	int l3 = INITIAL_BALANCE;
	int l4 = INITIAL_BALANCE;
	int l5 = INITIAL_BALANCE;
	int l6 = INITIAL_BALANCE;
	int l7 = INITIAL_BALANCE;
	int l8 = INITIAL_BALANCE;
	int l9 = INITIAL_BALANCE;
	int l10 = INITIAL_BALANCE;
	/*int l11 = INITIAL_BALANCE;
	int l12 = INITIAL_BALANCE;
	int l13 = INITIAL_BALANCE;
	int l14 = INITIAL_BALANCE;
	int l15 = INITIAL_BALANCE;
	int l16 = INITIAL_BALANCE;
	int l17 = INITIAL_BALANCE;
	int l18 = INITIAL_BALANCE;
	int l19 = INITIAL_BALANCE;
	int l20 = INITIAL_BALANCE;
	int l21 = INITIAL_BALANCE;
	int l22 = INITIAL_BALANCE;
	int l23 = INITIAL_BALANCE;
	int l24 = INITIAL_BALANCE;
	int l25 = INITIAL_BALANCE;
	int l26 = INITIAL_BALANCE;
	int l27 = INITIAL_BALANCE;
	int l28 = INITIAL_BALANCE;
	int l29 = INITIAL_BALANCE;
	int l30 = INITIAL_BALANCE;//comentar aqui
	int l31 = INITIAL_BALANCE;
	int l32 = INITIAL_BALANCE;
	int l33 = INITIAL_BALANCE;
	int l34 = INITIAL_BALANCE;
	int l35 = INITIAL_BALANCE;
	int l36 = INITIAL_BALANCE;
	int l37 = INITIAL_BALANCE;
	int l38 = INITIAL_BALANCE;
	int l39 = INITIAL_BALANCE;
	int l40 = INITIAL_BALANCE;
	int l41 = INITIAL_BALANCE;
	int l42 = INITIAL_BALANCE;
	int l43 = INITIAL_BALANCE;
	int l44 = INITIAL_BALANCE;
	int l45 = INITIAL_BALANCE;
	int l46 = INITIAL_BALANCE;
	int l47 = INITIAL_BALANCE;
	int l48 = INITIAL_BALANCE;
	int l49 = INITIAL_BALANCE;
	int l50 = INITIAL_BALANCE;
	int l51 = INITIAL_BALANCE;
	int l52 = INITIAL_BALANCE;
	int l53 = INITIAL_BALANCE;
	int l54 = INITIAL_BALANCE;
	int l55 = INITIAL_BALANCE;
	int l56 = INITIAL_BALANCE;
	int l57 = INITIAL_BALANCE;
	int l58 = INITIAL_BALANCE;
	int l59 = INITIAL_BALANCE;
	int l60 = INITIAL_BALANCE;
	int l61 = INITIAL_BALANCE;
	/*int l62 = INITIAL_BALANCE;
    int l63 = INITIAL_BALANCE;
    int l64 = INITIAL_BALANCE;
    int l65 = INITIAL_BALANCE;
    int l66 = INITIAL_BALANCE;
    int l67 = INITIAL_BALANCE;
    int l68 = INITIAL_BALANCE;
    int l69 = INITIAL_BALANCE;
    int l70 = INITIAL_BALANCE;
    int l71 = INITIAL_BALANCE;
    int l72 = INITIAL_BALANCE;
    int l73 = INITIAL_BALANCE;
    int l74 = INITIAL_BALANCE;
    int l75 = INITIAL_BALANCE;
    int l76 = INITIAL_BALANCE;
    int l77 = INITIAL_BALANCE;
    int l78 = INITIAL_BALANCE;
    int l79 = INITIAL_BALANCE;
    int l80 = INITIAL_BALANCE;
    int l81 = INITIAL_BALANCE;
    int l82 = INITIAL_BALANCE;
    int l83 = INITIAL_BALANCE;
    int l84 = INITIAL_BALANCE;
    int l85 = INITIAL_BALANCE;
    int l86 = INITIAL_BALANCE;
    int l87 = INITIAL_BALANCE;
    int l88 = INITIAL_BALANCE;
    int l89 = INITIAL_BALANCE;
    int l90 = INITIAL_BALANCE;
    int l91 = INITIAL_BALANCE;
    int l92 = INITIAL_BALANCE;
    int l93 = INITIAL_BALANCE;
    int l94 = INITIAL_BALANCE;
    int l95 = INITIAL_BALANCE;
    int l96 = INITIAL_BALANCE;
    int l97 = INITIAL_BALANCE;
    int l98 = INITIAL_BALANCE;
    int l99 = INITIAL_BALANCE;
    /*int l100 = INITIAL_BALANCE;
    int l101 = INITIAL_BALANCE;
    int l102 = INITIAL_BALANCE;
    int l103 = INITIAL_BALANCE;
    int l104 = INITIAL_BALANCE;
    int l105 = INITIAL_BALANCE;
    int l106 = INITIAL_BALANCE;
    int l107 = INITIAL_BALANCE;
    int l108 = INITIAL_BALANCE;
    int l109 = INITIAL_BALANCE;
    int l110 = INITIAL_BALANCE;
    int l111 = INITIAL_BALANCE;
    int l112 = INITIAL_BALANCE;
    int l113 = INITIAL_BALANCE;
    int l114 = INITIAL_BALANCE;
    int l115 = INITIAL_BALANCE;
    int l116 = INITIAL_BALANCE;
    int l117 = INITIAL_BALANCE;
    int l118 = INITIAL_BALANCE;
    int l119 = INITIAL_BALANCE;
    int l120 = INITIAL_BALANCE;
    int l121 = INITIAL_BALANCE;
    int l122 = INITIAL_BALANCE;
    int l123 = INITIAL_BALANCE;
    int l124 = INITIAL_BALANCE;
    int l125 = INITIAL_BALANCE;
    int l126 = INITIAL_BALANCE;
    int l127 = INITIAL_BALANCE;
    int l128 = INITIAL_BALANCE;
    int l129 = INITIAL_BALANCE;
    int l130 = INITIAL_BALANCE;
    int l131 = INITIAL_BALANCE;
    int l132 = INITIAL_BALANCE;
    int l133 = INITIAL_BALANCE;
    int l134 = INITIAL_BALANCE;
    int l135 = INITIAL_BALANCE;
    int l136 = INITIAL_BALANCE;
    int l137 = INITIAL_BALANCE;
    int l138 = INITIAL_BALANCE;
    int l139 = INITIAL_BALANCE;
    int l140 = INITIAL_BALANCE;
    int l141 = INITIAL_BALANCE;
    int l142 = INITIAL_BALANCE;
    int l143 = INITIAL_BALANCE;
    int l144 = INITIAL_BALANCE;
    int l145 = INITIAL_BALANCE;
    int l146 = INITIAL_BALANCE;
    int l147 = INITIAL_BALANCE;
    int l148 = INITIAL_BALANCE;
    int l149 = INITIAL_BALANCE;*/


	public BankBenchmark() {
	}

	public void transfer(int srcAccount, int dstAccount) {

		//if(srcAccount>0 && dstAccount>0){
			//lock.lock();
		//}
		
		int srcAmount = get(Math.abs(srcAccount % numAccounts));
		int amountToTransfer = srcAmount / 10;

		set(Math.abs(srcAccount % numAccounts), srcAmount - amountToTransfer);

		int dstAmount = get(Math.abs(dstAccount % numAccounts));
		set(Math.abs(dstAccount % numAccounts), dstAmount + amountToTransfer);	
		//if(srcAccount>0 && dstAccount>0){
			//lock.unlock();
		//}
	}


	public int sumBalances() {
		int total = 0;
		for(int i = 0; i < numAccounts; i++)
		{
			total += get(i);
		}
		return total;
	}


	public boolean checkBalances() {
		lock.lock();
		int sum = sumBalances();
		if (sum != (INITIAL_BALANCE * numAccounts)) {
			System.out.printf("[Bug] The sumBalances returned a value (%d) different than it should (%d)!\n", sum, (INITIAL_BALANCE * numAccounts));
			lock.unlock();
			return false;
		}
		lock.unlock();
		return true;
	}


	public TestThread createThread() {
		try {
			TestThread testThread = new TestThread(this);
			return testThread;
		} catch (Exception e) {
			e.printStackTrace(System.out);
			return null;
		}
	}


	int get(int index)
	{
		switch(index)
		{
		case 0: return l0;
		case 1: return l1;
		case 2: return l2;
		case 3: return l3;
		case 4: return l4;
		case 5: return l5;
		case 6: return l6;
		case 7: return l7;
		case 8: return l8;
		case 9: return l9;
		case 10: return l10;
		/*case 11: return l11;
		case 12: return l12;
		case 13: return l13;
		case 14: return l14;
		case 15: return l15;
		case 16: return l16;
		case 17: return l17;
		case 18: return l18;
		case 19: return l19;
		case 20: return l20;
		case 21: return l21;
		case 22: return l22;
		case 23: return l23;
		case 24: return l24;
		case 25: return l25;
		case 26: return l26;
		case 27: return l27;
		case 28: return l28;
		case 29: return l29;
		case 30: return l30;//comentar aqui
		case 31: return l31;
		case 32: return l32;
		case 33: return l33;
		case 34: return l34;
		case 35: return l35;
		case 36: return l36;
		case 37: return l37;
		case 38: return l38;
		case 39: return l39;
		case 40: return l40;
		case 41: return l41;
		case 42: return l42;
		case 43: return l43;
		case 44: return l44;
		case 45: return l45;
		case 46: return l46;
		case 47: return l47;
		case 48: return l48;
		case 49: return l49;
		case 50: return l50;
		case 51: return l51;
		case 52: return l52;
		case 53: return l53;
		case 54: return l54;
		case 55: return l55;
		case 56: return l56;
		case 57: return l57;
		case 58: return l58;
		case 59: return l59;
		case 60: return l60;
		case 61: return l61;
		/*case 62: return l62;
	case 63: return l63;
	case 64: return l64;
	case 65: return l65;
	case 66: return l66;
	case 67: return l67;
	case 68: return l68;
	case 69: return l69;
	case 70: return l70;
	case 71: return l71;
	case 72: return l72;
	case 73: return l73;
	case 74: return l74;
	case 75: return l75;
	case 76: return l76;
	case 77: return l77;
	case 78: return l78;
	case 79: return l79;
	case 80: return l80;
	case 81: return l81;
	case 82: return l82;
	case 83: return l83;
	case 84: return l84;
	case 85: return l85;
	case 86: return l86;
	case 87: return l87;
	case 88: return l88;
	case 89: return l89;
	case 90: return l90;
	case 91: return l91;
	case 92: return l92;
	case 93: return l93;
	case 94: return l94;
	case 95: return l95;
	case 96: return l96;
	case 97: return l97;
	case 98: return l98;
	case 99: return l99;
	/*case 100: return l100;
	case 101: return l101;
	case 102: return l102;
	case 103: return l103;
	case 104: return l104;
	case 105: return l105;
	case 106: return l106;
	case 107: return l107;
	case 108: return l108;
	case 109: return l109;
	case 110: return l110;
	case 111: return l111;
	case 112: return l112;
	case 113: return l113;
	case 114: return l114;
	case 115: return l115;
	case 116: return l116;
	case 117: return l117;
	case 118: return l118;
	case 119: return l119;
	case 120: return l120;
	case 121: return l121;
	case 122: return l122;
	case 123: return l123;
	case 124: return l124;
	case 125: return l125;
	case 126: return l126;
	case 127: return l127;
	case 128: return l128;
	case 129: return l129;
	case 130: return l130;
	case 131: return l131;
	case 132: return l132;
	case 133: return l133;
	case 134: return l134;
	case 135: return l135;
	case 136: return l136;
	case 137: return l137;
	case 138: return l138;
	case 139: return l139;
	case 140: return l140;
	case 141: return l141;
	case 142: return l142;
	case 143: return l143;
	case 144: return l144;
	case 145: return l145;
	case 146: return l146;
	case 147: return l147;
	case 148: return l148;
	case 149: return l149;*/
		}
		return -1;
	}


	void set(int index,int value)
	{
		switch(index)
		{
		case 0: l0=value; break;
		case 1: l1=value;break;
		case 2: l2=value;break;
		case 3: l3=value;break;
		case 4: l4=value;break;
		case 5: l5=value;break;
		case 6: l6=value;break;
		case 7: l7=value;break;
		case 8: l8=value;break;
		case 9: l9=value;break;
		case 10: l10=value;break;
		/*case 11: l11=value;break;
		case 12: l12=value;break;
		case 13: l13=value;break;
		case 14: l14=value;break;
		case 15: l15=value;break;
		case 16: l16=value;break;
		case 17: l17=value;break;
		case 18: l18=value;break;
		case 19: l19=value;break;
		case 20: l20=value;break;
		case 21: l21=value;break;
		case 22: l22=value;break;
		case 23: l23=value;break;
		case 24: l24=value;break;
		case 25: l25=value;break;
		case 26: l26=value;break;
		case 27: l27=value;break;
		case 28: l28=value;break;
		case 29: l29=value;break;
		case 30: l30=value;break;
		case 31: l31=value;break;
		case 32: l32=value;break;
		case 33: l33=value;break;
		case 34: l34=value;break;
		case 35: l35=value;break;
		case 36: l36=value;break;
		case 37: l37=value;break;
		case 38: l38=value;break;
		case 39: l39=value;break;
		case 40: l40=value;break;
		case 41: l41=value;break;
		case 42: l42=value;break;
		case 43: l43=value;break;
		case 44: l44=value;break;
		case 45: l45=value;break;
		case 46: l46=value;break;
		case 47: l47=value;break;
		case 48: l48=value;break;
		case 49: l49=value;break;
		case 50: l50=value;break;
		case 51: l51=value;break;
		case 52: l52=value;break;
		case 53: l53=value;break;
		case 54: l54=value;break;
		case 55: l55=value;break;
		case 56: l56=value;break;
		case 57: l57=value;break;
		case 58: l58=value;break;
		case 59: l59=value;break;
		case 60: l60=value;break;
		case 61: l61=value;break;
		/*case 62: l62=value;break;
	case 63: l63=value;break;
	case 64: l64=value;break;
	case 65: l65=value;break;
	case 66: l66=value;break;
	case 67: l67=value;break;
	case 68: l68=value;break;
	case 69: l69=value;break;
	case 70: l70=value;break;
	case 71: l71=value;break;
	case 72: l72=value;break;
	case 73: l73=value;break;
	case 74: l74=value;break;
	case 75: l75=value;break;
	case 76: l76=value;break;
	case 77: l77=value;break;
	case 78: l78=value;break;
	case 79: l79=value;break;
	case 80: l80=value;break;
	case 81: l81=value;break;
	case 82: l82=value;break;
	case 83: l83=value;break;
	case 84: l84=value;break;
	case 85: l85=value;break;
	case 86: l86=value;break;
	case 87: l87=value;break;
	case 88: l88=value;break;
	case 89: l89=value;break;
	case 90: l90=value;break;
	case 91: l91=value;break;
	case 92: l92=value;break;
	case 93: l93=value;break;
	case 94: l94=value;break;
	case 95: l95=value;break;
	case 96: l96=value;break;
	case 97: l97=value;break;
	case 98: l98=value;break;
	case 99: l99=value;break;
	/*case 100: l100=value;break;
	case 101: l101=value;break;
	case 102: l102=value;break;
	case 103: l103=value;break;
	case 104: l104=value;break;
	case 105: l105=value;break;
	case 106: l106=value;break;
	case 107: l107=value;break;
	case 108: l108=value;break;
	case 109: l109=value;break;
	case 110: l110=value;break;
	case 111: l111=value;break;
	case 112: l112=value;break;
	case 113: l113=value;break;
	case 114: l114=value;break;
	case 115: l115=value;break;
	case 116: l116=value;break;
	case 117: l117=value;break;
	case 118: l118=value;break;
	case 119: l119=value;break;
	case 120: l120=value;break;
	case 121: l121=value;break;
	case 122: l122=value;break;
	case 123: l123=value;break;
	case 124: l124=value;break;
	case 125: l125=value;break;
	case 126: l126=value;break;
	case 127: l127=value;break;
	case 128: l128=value;break;
	case 129: l129=value;break;
	case 130: l130=value;break;
	case 131: l131=value;break;
	case 132: l132=value;break;
	case 133: l133=value;break;
	case 134: l134=value;break;
	case 135: l135=value;break;
	case 136: l136=value;break;
	case 137: l137=value;break;
	case 138: l138=value;break;
	case 139: l139=value;break;
	case 140: l140=value;break;
	case 141: l141=value;break;
	case 142: l142=value;break;
	case 143: l143=value;break;
	case 144: l144=value;break;
	case 145: l145=value;break;
	case 146: l146=value;break;
	case 147: l147=value;break;
	case 148: l148=value;break;
	case 149: l149=value;break;*/
		}
		return; 
	}



}
