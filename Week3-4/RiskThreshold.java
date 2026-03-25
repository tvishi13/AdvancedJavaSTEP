public class RiskThreshold {

    static int linearSearch(int[] arr, int target) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == target) return i;
        }
        return -1;
    }

    static int floor(int[] arr, int target) {
        int res = -1;
        for (int num : arr) {
            if (num <= target) res = num;
        }
        return res;
    }

    static int ceiling(int[] arr, int target) {
        for (int num : arr) {
            if (num >= target) return num;
        }
        return -1;
    }

    static int insertionPoint(int[] arr, int target) {
        int low = 0, high = arr.length;

        while (low < high) {
            int mid = (low + high) / 2;
            if (arr[mid] < target)
                low = mid + 1;
            else
                high = mid;
        }
        return low;
    }
}
