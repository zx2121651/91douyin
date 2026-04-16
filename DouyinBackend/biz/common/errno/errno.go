package errno

import (
	"errors"
	"fmt"
)

type ErrNo struct {
	StatusCode int32
	StatusMsg  string
}

func (e ErrNo) Error() string {
	return fmt.Sprintf("err_code=%d, err_msg=%s", e.StatusCode, e.StatusMsg)
}

func NewErrNo(code int32, msg string) ErrNo {
	return ErrNo{
		StatusCode: code,
		StatusMsg:  msg,
	}
}

func (e ErrNo) WithMessage(msg string) ErrNo {
	e.StatusMsg = msg
	return e
}

var (
	Success    = NewErrNo(0, "Success")
	ServiceErr = NewErrNo(10001, "Service Error")
	ParamErr   = NewErrNo(10002, "Parameter Error")
	AuthErr    = NewErrNo(10003, "Authentication Error")
	ForbiddenErr = NewErrNo(10004, "Forbidden Error")
	NotFoundErr  = NewErrNo(10005, "Resource Not Found")

	UserNotFoundErr    = NewErrNo(20001, "User Not Found")
	UserAlreadyExistErr = NewErrNo(20002, "User Already Exist")
	PasswordErr        = NewErrNo(20003, "Password Error")

	VideoNotFoundErr   = NewErrNo(30001, "Video Not Found")

	FavoriteActionErr  = NewErrNo(40001, "Favorite Action Error")
	CommentActionErr   = NewErrNo(50001, "Comment Action Error")
	RelationActionErr  = NewErrNo(60001, "Relation Action Error")
	MessageActionErr   = NewErrNo(70001, "Message Action Error")
)

// ConvertErr convert error to ErrNo
func ConvertErr(err error) ErrNo {
	if err == nil {
		return Success
	}

	var e ErrNo
	if errors.As(err, &e) {
		return e
	}

	s := ServiceErr
	s.StatusMsg = err.Error()
	return s
}
