package kr.co.signallink.svsv2.user;

public class RegisterSVSItem extends RegisterSVSData implements Comparable {

    private boolean checked = false;

    //////////////////////////////////////////////////

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    //////////////////////////////////////////////////
    //Comparable


    @Override
    public int compareTo(Object o) {

        //이름이 다르면, 이름 순 정렬
        //이름이 같으면, Address 순 정렬

        RegisterSVSItem item = (RegisterSVSItem)o;
        if(!this.getName().equals(item.getName()))
        {
            return this.getName().compareTo(item.getName());
        }
        else
        {
            return this.getAddress().compareTo(item.getAddress());
        }

    }
}
