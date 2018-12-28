package br.com.frsiqueira.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;

public class Payment {
    private Integer id;
    private Integer parcel;
    private Date date;
    private String type;
    private BigDecimal amount;
    private boolean paid;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getParcel() {
        return parcel;
    }

    public void setParcel(Integer parcel) {
        this.parcel = parcel;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", parcel=" + parcel +
                ", date=" + date +
                ", type='" + type + '\'' +
                ", amount=" + amount +
                ", paid=" + paid +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return paid == payment.paid &&
                Objects.equals(id, payment.id) &&
                Objects.equals(parcel, payment.parcel) &&
                Objects.equals(date, payment.date) &&
                Objects.equals(type, payment.type) &&
                Objects.equals(amount, payment.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, parcel, date, type, amount, paid);
    }
}
